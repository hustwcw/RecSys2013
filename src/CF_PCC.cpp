//
//  CF_PCC.cpp
//  RecSys2013
//
//  Created by JTangWang on 13-8-12.
//  Copyright (c) 2013年 JTangWang. All rights reserved.
//

#include "CF_PCC.h"
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


#include "BasicPMF.h"
#include "BiasSVD.h"
#include "Util.h"
#include "SparseMatrix.h"
#include "User.h"
#include "Business.h"
#include "Review.h"
#include "Category.h"

using namespace std;



map<string, float> result;  // 评分预测结果，key为uid与bid的连接


template<class T, class V>
void PCC(const SparseMatrix<float> &sparseM,
         map<string, T> &rowMap,
         const map<string, V> &colMap,
         const vector<V> &colVec,
         const multimap<string, string> &predictionMap)
{
    int insertSimCount = 0;
    int discardSimCount = 0;
    // 对于每个用户，都计算他与其他用户的相似度
    int row = 0;
    int resultInsertCount = 0;
    for (typename map<string, T>::iterator userIter1 = rowMap.begin(); userIter1 != rowMap.end(); ++userIter1) {
        if ((++row)%1024 == 0) {
            cout << row << endl;
        }
        // 不在训练集的review中的数据不需要计算相似度
        if (userIter1->second.sequence < 0)
        {
            continue;
        }
        // 每个用户都有一个相似度map，记录该用户与其他用户之间的相似度
        map<string, float> simMap; // uid-sim Map
        for (typename map<string, T>::iterator userIter2 = rowMap.begin(); userIter2 != rowMap.end(); ++userIter2) {
            if (userIter2 == userIter1 || userIter2->second.sequence < 0) {
                continue;
            }
            // 计算iter1和iter2的相似度
            float nominator = 0.0f;
            float denominator1 = 0.0f;
            float denominator2 = 0.0f;
            int intersectCount = 0;
            int row1 = userIter1->second.sequence;
            int row2 = userIter2->second.sequence;
            int i = sparseM.rpos[row1];
            int j = sparseM.rpos[row2];
            for (; i < sparseM.rpos[row1+1] && j < sparseM.rpos[row2+1]; ) {
                if (sparseM.data[i].j == sparseM.data[j].j) {
                    // 列号相同，计算相似度
                    float tempa = sparseM.data[i].elem - userIter1->second.avgStar;
                    float tempu = sparseM.data[j].elem - userIter2->second.avgStar;
                    //                    float tempa = sparseM.data[i].elem - colVec[sparseM.data[i].j].avgStar;
                    //                    float tempu = sparseM.data[j].elem - colVec[sparseM.data[i].j].avgStar;
                    // 需要实验确定对数的底是2好还是10好
                    //                    assert(colVec[sparseM.data[i].j].sequence == sparseM.data[i].j);
                    //                    float popWeight = 1.0/log(1.0 + colVec[sparseM.data[i].j].reviewCount); // 对热门商品进行惩罚的权值 1/log(1+N(i))
                    nominator += tempa * tempu; // * popWeight; 该权重不能提高预测的准确度
                    denominator1 += tempa * tempa;
                    denominator2 += tempu * tempu;
                    intersectCount++;
                    ++i;
                    ++j;
                }
                else if (sparseM.data[i].j < sparseM.data[j].j)
                {
                    ++i;
                }
                else// if (sparseM->data[i].j > sparseM->data[j].j)
                {
                    ++j;
                }
            }
            
            if (nominator > 0 && denominator1 > 0 && denominator2 > 0) {
                float weight = (2.0 * intersectCount / (userIter1->second.reviewCount + userIter2->second.reviewCount));
                float sim = weight * (nominator / sqrt(denominator1 * denominator2));
                if (sim > 0.005) {
                    ++insertSimCount;
                    simMap.insert(make_pair(userIter2->first, sim));
                }
                else
                {
                    ++discardSimCount;
                }
            }
        }
        
        // 根据simMap计算User的confident weight
        float simSum = 0;
        float nominator = 0;
        for (map<string, float>::iterator iter = simMap.begin(); iter != simMap.end(); ++iter) {
            simSum += iter->second;
        }
        for (map<string, float>::iterator iter = simMap.begin(); iter != simMap.end(); ++iter) {
            nominator += (iter->second) * (iter->second);
        }
        if (nominator >0 && simSum > 0) {
            userIter1->second.confident = nominator / simSum;
        }
        else
        {
            userIter1->second.confident = 0;
        }
        
        // 针对userIter1用户，找出所有需要预测的business
        pair<multimap<string, string>::const_iterator, multimap<string, string>::const_iterator> ret = predictionMap.equal_range(userIter1->first);
        for (multimap<string, string>::const_iterator rangeIter = ret.first; rangeIter != ret.second; ++rangeIter) {
            typename map<string, V>::const_iterator businessIter = colMap.find(rangeIter->second);
            // 需要预测的business不在训练集中
            if (businessIter == colMap.end()) {
                continue;
            }
            int col = businessIter->second.sequence;
            float nominator = 0;
            float denominator = 0;
            for (map<string, float>::iterator simIter = simMap.begin(); simIter != simMap.end(); ++simIter) {
                typename map<string, T>::iterator userIter = rowMap.find(simIter->first);
                int row = userIter->second.sequence;
                float uaStar = sparseM.getElem(row, col);
                // 有相似度的用户对于该要预测的商家没有过review
                if (uaStar <= 0) {
                    continue;
                }
                float temp = uaStar - userIter->second.avgStar;
                //                if (temp < 0) {
                //                    temp *= 0.25;
                //                }
                nominator += (simIter->second * temp);
                denominator += simIter->second;
            }
            
            if (denominator > 0) {
                if (nominator < 0) {
                    // 对低于平均值的打分进行降权
                    nominator /= 8;
                }
                float predictStar = rowMap.find(userIter1->first)->second.avgStar + (nominator/denominator);
                // 将计算结果插入到result中
                result.insert(make_pair(rangeIter->first+rangeIter->second, predictStar));
                ++resultInsertCount;
            }
        }
    }
    
    cout << "Insert Sim Count: " << insertSimCount << "\tDiscard Sim Count: " << discardSimCount << endl;
    cout << "PCC: Result Insert Count:" << resultInsertCount << "\tResult Count:" << result.size() << endl;
}


void UIPCC(const map<string, User> &userMap, const map<string, Business> &businessMap, float lamda)
{
    stringstream predictionFileName;

    
    ifstream  testReviewFile = ifstream(FolderName + "final_test_set/final_test_set_review.json");
    predictionFileName << FolderName << "PCC/PCC_lamda_" << lamda << ".csv";

    
    ofstream predictionFile = ofstream(predictionFileName.str());
    // 根据result和UserMap、BusinessMap中的评分平均值计算最终的评分
    if (testReviewFile.is_open())
    {
        string line;
        predictionFile << "review_id,stars" << endl;
        int upccCount = 0;
        int ipccCount = 0;
        while (!testReviewFile.eof())
        {
            getline(testReviewFile, line);
            string uid = line.substr(13, 22);
            string reviewid = line.substr(52, 22);
            string bid = line.substr(93, 22);
            
            map<string, User>::const_iterator userIter = userMap.find(uid);
            map<string, Business>::const_iterator businessIter = businessMap.find(bid);
            map<string, float>::iterator userResultIter = result.find(uid+bid);
            map<string, float>::iterator busiResultIter = result.find(bid+uid);
            float upcc, ipcc;
            float prediction;
            bool userInTraining = true;
            bool businessInTraining = true;
            
            if (userResultIter != result.end()) {
                upcc = userResultIter->second;
                ++upccCount;
            }
            else
            {
                if (userIter != userMap.end()) {
                    upcc = userIter->second.avgStar;
                }
                else
                {
                    // 该用户不在训练集中
                    userInTraining = false;
                }
            }
            
            if (busiResultIter != result.end()) {
                ipcc = busiResultIter->second;
                ++ipccCount;
            }
            else
            {
                if (businessIter != businessMap.end()) {
                    ipcc = businessIter->second.avgStar;
                }
                else
                {
                    // 该商家不在训练集中
                    businessInTraining = false;
                }
            }
            
            if (userInTraining && businessInTraining) {
                // 都在训练集中
                float wu, wi;
                float conu = userIter->second.confident;
                float coni = businessIter->second.confident;
                if (conu == 0 || coni == 0) {
                    // TODO:这里需要更好的利用conu和coni的方法
                    if (conu != 0) {
                        wu = lamda; // + conu/8;
                        wi = 1 - wu;
                    }
                    else if (coni != 0)
                    {
                        wu = lamda; // - coni/8;
                        wi = 1-wu;
                    }
                    else
                    {
                        wu = lamda;
                        wi = 1-lamda;
                    }
                }
                else
                {
                    wu = (conu * lamda) / (conu*lamda + coni*(1-lamda));
                    wi = (coni * (1-lamda)) / (conu*lamda + coni*(1-lamda));
                }
                prediction = wu * upcc + wi *ipcc;
            }
            else if (userInTraining)
            {
                // 只有用户在训练集中
                prediction = upcc;
            }
            else if (businessInTraining)
            {
                // 只有商家在训练集中
                prediction = ipcc;
            }
            else
            {
                // 用户和商家都不在训练集中
                prediction = 3.67452543988905;
            }
            if (prediction > 5) {
                prediction = 5;
            }
            if (prediction < 1) {
                prediction = 1;
            }
            predictionFile << reviewid << "," << prediction << endl;
        }
        
        cout << "UPCC Count: " << upccCount << "\tIPCC Count: " << ipccCount << endl;
    }
    testReviewFile.close();
    predictionFile.close();
}



void TestCFPCC(const SparseMatrix<float> &sparseUBMatrix,
               const SparseMatrix<float> &sparseBUMatrix,
               map<string, User> &userMap,
               map<string, Business> &businessMap,
               const multimap<string, string> &predictionUBMap,
               const multimap<string, string> &predictionBUMap)
{
    vector<User> userVec(userMap.size());
    vector<Business> businessVec(businessMap.size());
    // 根据userMap和businessMap生成userVec、businessVec
    for (map<string, User>::const_iterator iter = userMap.begin(); iter != userMap.end(); ++iter) {
        if (iter->second.sequence >= 0) {
            userVec[iter->second.sequence] = iter->second;
        }
    }
    for (map<string, Business>::const_iterator iter = businessMap.begin(); iter != businessMap.end(); ++iter) {
        if (iter->second.sequence >= 0) {
            businessVec[iter->second.sequence] = iter->second;
        }
    }
    
    PCC(sparseUBMatrix, userMap, businessMap, businessVec, predictionUBMap); // UPCC
    PCC(sparseBUMatrix, businessMap, userMap, userVec, predictionBUMap); // IPCC
    
    float lamda = 0;
    for (int i = 0; i < 21; ++i) {
        UIPCC(userMap, businessMap, lamda);
        lamda += 0.05;
    }
}