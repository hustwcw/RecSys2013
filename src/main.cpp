//
//  main.cpp
//  RecSys2013
//
//  Created by JTangWang on 13-7-5.
//  Copyright (c) 2013年 JTangWang. All rights reserved.
//

#include <iostream>
#include <fstream>
#include <set>
#include <map>
#include <string>
#include <cstdlib>
#include <vector>
#include <list>
#include <algorithm>
#include <cmath>
#include <sstream>
#include <assert.h>


#include <boost/scoped_ptr.hpp>
#include <boost/lexical_cast.hpp>
#include <boost/property_tree/ptree.hpp>
#include <boost/property_tree/json_parser.hpp>


#include "CF_PCC.h"
#include "BasicPMF.h"
#include "BiasSVD.h"
#include "Util.h"
#include "SparseMatrix.h"
#include "User.h"
#include "Business.h"
#include "Review.h"
#include "Category.h"



using namespace std;
using namespace boost::property_tree;


// 加载训练集数据，在加载评分数据的过程中加载用户和商家数据
void loadTrainingSet(map<string, User> &userMap, map<string, Business> &businessMap, set<Review> &reviewSet, const map<string, Category> &categoryMap)
{
    ifstream trainingReviewFile = ifstream(FolderName + "yelp_training_set/yelp_training_set_review.json");

    // 对于training_set_review根据uid和bid进行排序，便于直接生成稀疏矩阵
    if (trainingReviewFile.is_open()) {
        while (!trainingReviewFile.eof()) {
            string line;
            getline(trainingReviewFile, line);
            size_t start = line.find("\"user_id\"");
            string uid = line.substr(start+12, 22);
            string bid = line.substr(line.length() - 24, 22);
            start = line.find("\"stars\"", 124);
            int stars = atoi(line.substr(start+9, 1).c_str());
            start = line.find("\"date\":", 124);
            int year = atoi(line.substr(start+9, 4).c_str());
            int month = atoi(line.substr(start+14, 2).c_str());
            int day = atoi(line.substr(start+17, 2).c_str());
            int date = year*365+month*30+day;
            // 判断用户id是否已经出现在userMap中，如果没有则将新出现的用户数据插入到userMap中
            // 否则，修改userMap中对应的用户数据（reviewCount和平均分）
            map<string, User>::iterator userIter = userMap.find(uid);
            if (userIter == userMap.end()) {
                userMap.insert(make_pair(uid, User(0, stars, 1, "")));
            }
            else
            {
                // 先计算总得打分，最后计算平均分，business类似
                ++(userIter->second.reviewCount);
                userIter->second.starVec.push_back(stars);
            }
            // businessMap的更新和userMap类似
            map<string, Business>::iterator businessIter = businessMap.find(bid);
            if (businessIter == businessMap.end()) {
                businessMap.insert(make_pair(bid, Business(0, stars, 0, 1, "")));
            }
            else
            {
                ++(businessIter->second.reviewCount);
                businessIter->second.starVec.push_back(stars);
            }
            
            reviewSet.insert(Review(uid, bid, stars, date));
        }
    }
    else
    {
        cout << "can't open trainingReviewFile" << endl;
    }
    trainingReviewFile.close();
    
    
    // 根据用户ID的字符串顺序调整用户在矩阵中的位置
    // 计算用户打分的平均分以及打分的RMSE
    int sequence = 0;
    for (map<string, User>::iterator iter = userMap.begin(); iter != userMap.end(); ++iter) {
        iter->second.avgStar = calculateVectorAvg(iter->second.starVec);
        iter->second.RMSE = calculateVectorRMSE(iter->second.starVec, iter->second.avgStar);
        iter->second.sequence = sequence++;
    }
    
    // 根据商家ID的字符串顺序调整商家在矩阵中的位置
    // 计算商家打分的平均分以及打分的RMSE
    sequence = 0;
    for (map<string, Business>::iterator iter = businessMap.begin(); iter != businessMap.end(); ++iter) {
        // 没有review_count小于3的business，等于3的有2531
        iter->second.avgStar = calculateVectorAvg(iter->second.starVec);
        iter->second.RMSE = calculateVectorRMSE(iter->second.starVec, iter->second.avgStar);
        iter->second.sequence = sequence++;
    }
    
    // load training_set_user to userMap
    // 用户数据文件中的数据修改上一步得到的用户数据，评分数和平均分以用户文件中的数据为准
    ifstream trainingSetUserFile(FolderName + "yelp_training_set/yelp_training_set_user.json");
    if (trainingSetUserFile.is_open()) {
        while (!trainingSetUserFile.eof()) {
            string line;
            getline(trainingSetUserFile, line);
            size_t start, end;
            start = line.find("\"user_id\":", 48);
            string uid = line.substr(start+12, 22);
            
            start = line.find("\"name\"", 80);
            end = line.find(",", start);
            string name = line.substr(start + 9, end - start - 10);
            
            start = line.find("\"average_stars\"", 96);
            end = line.find(",", start+17);
            float avg_stars = atof(line.substr(start+17, end - start - 17).c_str());
            start = line.find("\"review_count\"", end);
            end = line.find(",", start);
            int review_count = atoi(line.substr(start+16, end-start-16).c_str());
            map<string, User>::iterator userIter = userMap.find(uid);
            // if (userIter != userMap.end()) 一定为真
            // id 为：KQnq1p-PlWUo-qPEtWtmMw 的用户在training_set_user里面的平均分是0，review_count是17；
            // 从review里计算的平均分是3,review_cnt是1
            if (avg_stars > 0) {
                // TESTK
                userIter->second.avgStar = avg_stars;
                userIter->second.reviewCount = review_count;
                userIter->second.name = name;
            }
        }
    }
    else
    {
        cout << "can't open trainingSetUserFile" << endl;
    }
    trainingSetUserFile.close();
    
    // load training_set_business to businessMap
    int lessBusiCnt = 0;
    ifstream trainingSetBusinessFile(FolderName + "yelp_training_set/yelp_training_set_business.json");
    if (trainingSetBusinessFile.is_open()) {
        while (!trainingSetBusinessFile.eof()) {
            vector<string> category;
            string line;
            getline(trainingSetBusinessFile, line);
            stringstream jsonStream(line);
            ptree pt;
            read_json(jsonStream, pt);
            ptree ptCategory = pt.get_child("categories");
            float weight = 0;
            float totalStar = 0;
            for (ptree::iterator iter = ptCategory.begin(); iter != ptCategory.end(); ++iter) {
                string cate(iter->second.data());
                map<string, Category>::const_iterator cateIter = categoryMap.find(cate);
                // 部分类别不存在
                if (cateIter != categoryMap.end())
                {
                    float avgStar = cateIter->second.avgStar;
                    int reviewCnt = cateIter->second.reviewCnt;
                    float RMSE = cateIter->second.RMSE;
                    // TODO: 存在RMSE为零但是review_count比较多的情况
                    if (RMSE < GlobalRMSE && RMSE != 0)
                    {
                        weight += log10(reviewCnt)/RMSE;
                        totalStar += (avgStar * log10(reviewCnt) / RMSE);
                    }
                }
                else
                {
                    // 打印出不存在的类别信息
                    // cout << cate << endl;
                }
            }
            if (totalStar == 0) {
                totalStar = GlobalAvg;
            }
            else
            {
                totalStar /= weight;
            }
            
            string bid = pt.get<string>("business_id");
            float avg_stars = pt.get<float>("stars");
            int review_count = pt.get<int>("review_count");
            string city = pt.get<string>("city");
            
            map<string, Business>::iterator businessIter = businessMap.find(bid);
            // if (businessIter != businessMap.end()) 一定为真
            // 如果review文件中的review_count小于10而且business文件中的review_count大于review中的review_count，
            // 则使用business文件中的review_count和avg_star
            businessIter->second.city = city;
            businessIter->second.cateAvgStar = totalStar;
            // 有下面的调整时结果是：1.24577；没有调整的结果是：1.24588
            if (businessIter->second.reviewCount < 10 && review_count > businessIter->second.reviewCount*1.3) {
                businessIter->second.avgStar = avg_stars;
                businessIter->second.reviewCount = review_count;
                lessBusiCnt++;
            }
        }
    }
    else
    {
        cout << "can't open trainingSetBusinessFile" << endl;
    }
    trainingSetBusinessFile.close();
    cout << "less business Count:" << lessBusiCnt << endl;

    
    // 根据用户的review_count和avg_star重新计算平均分
    float K = 0;
    int uc1 = 0, uc2 = 0, uc3 = 0, uc4 = 0;
    for (map<string, User>::iterator iter = userMap.begin(); iter != userMap.end(); ++iter) {
        if (iter->second.reviewCount == 1) {
            K = 5;
            ++uc1;
        }
        else if (iter->second.reviewCount == 2)
        {
            K = 2.5*(iter->second.RMSE/20 + 1);
            ++uc2;
        }
        else if (iter->second.reviewCount == 3)
        {
            K = 1.5*(iter->second.RMSE/30 + 1);
            ++uc3;
        }
        else// if (iter->second.reviewCount == 4)
        {
            K = 0;
            ++uc4;
        }
        iter->second.avgStar = (GlobalAvg*K + iter->second.avgStar*iter->second.reviewCount) / (K + iter->second.reviewCount);
    }
    cout << uc1 << "\t" << uc2 << "\t" << uc3 << "\t" << uc4 << endl;
    
    for (map<string, Business>::iterator iter = businessMap.begin(); iter != businessMap.end(); ++iter) {
        if (iter->second.reviewCount == 3) {
            // K = 0.4      1.24580
            // K = 0.6      1.24466
            K = 0.6;
            iter->second.avgStar = (GlobalAvg*K + iter->second.avgStar*iter->second.reviewCount) / (K + iter->second.reviewCount);
        }
    }
}


void generateMatrix(SparseMatrix<std::pair<float, int> > &sparseM, const map<string, User> &userMap, const map<string, Business> &businessMap, const set<Review> &reviewSet)
{
    // 根据reviewSet生成稀疏矩阵
    int index = 0;
    int lastRow = -1;
    for (set<Review>::iterator iter = reviewSet.begin(); iter != reviewSet.end(); ++iter) {
        int row = userMap.find(iter->uid)->second.sequence;
        if (row != lastRow) {
            sparseM.rpos[row] = index;
        }
        int col = businessMap.find(iter->bid)->second.sequence;
		sparseM.data[index++] = Triple<std::pair<float, int> >(row, col, make_pair(iter->star, iter->date));
        lastRow = row;
    }
    sparseM.rpos[lastRow+1] = index;
}



int main(int argc, const char * argv[])
{
    map<string, User> userMap;
    map<string, Business> businessMap;
    map<string, Business> testBusinessMap;
    set<Review> reviewSet;
    map<string, bool> genderMap;
    map<string, float> cityAvgMap;
    multimap<string, string> predictionUBMap;     // 需要预测的uid和bid
    multimap<string, string> predictionBUMap;     // 需要预测的bid和uid

    map<string, Category> categoryMap; // 商家类别信息
    
    loadDataToPredict(predictionUBMap, predictionBUMap);
    loadGenderFile(genderMap);
    loadCategory(categoryMap);
    loadTrainingSet(userMap, businessMap, reviewSet, categoryMap);
    loadTestBusiness(testBusinessMap, categoryMap);
    loadCityAvg(cityAvgMap);
    
    
    // 生成稀疏矩阵
    int rowCount = static_cast<int>(userMap.size());
    int colCount = static_cast<int>(businessMap.size());
    SparseMatrix<std::pair<float, int> > sparseUBMatrix(static_cast<int>(reviewSet.size()), rowCount, colCount);
    SparseMatrix<std::pair<float, int> > sparseBUMatrix(static_cast<int>(reviewSet.size()), colCount, rowCount);
    generateMatrix(sparseUBMatrix, userMap, businessMap, reviewSet);
    sparseUBMatrix.transposeMatrix(sparseBUMatrix);
    
    
    // 测试协同过滤算法
    TestCFPCC(sparseUBMatrix, sparseBUMatrix, userMap, businessMap, predictionUBMap, predictionBUMap);
    return 0;
    
    
//    for (float lrate = 0.00045; lrate < 0.00046; lrate += 0.00005) {
//        for (int factor = 22; factor < 23; ++factor) {
//            cout << "lrate: " << lrate << "\tfactor: " << factor << endl;
//            BasicPMF pmf(rowCount, colCount, lrate, factor);
//            pmf.compute(sparseUBMatrix, sparseBUMatrix, 100, userMap, businessMap);
//            pmf.predict(userMap, businessMap, testBusinessMap, cityAvgMap);
//        }
//    }

//    analyzeGenderDistribution(genderMap, userMap);
//    
//    return 0;
    

    for (float lrate = 0.0004; lrate < 0.0005; lrate += 0.0002) {
        for (int factor = 20; factor < 100; factor+=100) {
            cout << "lrate: " << lrate << "\tfactor: " << factor << endl;
            BiasSVD biasSVD(rowCount, colCount, lrate, factor);
            biasSVD.compute(sparseUBMatrix, sparseBUMatrix, 1000);
            biasSVD.predict(userMap, businessMap, testBusinessMap);
        }
    }

    
    return 0;
}

