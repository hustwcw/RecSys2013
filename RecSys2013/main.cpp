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

#include "BasicPMF.h"
#include "Util.h"
#include "SparseMatrix.h"


#define LocalTest


using namespace std;




struct User {
    int sequence;
    float avgStar;
    int reviewCount;
    float confident;
    
    User(int theSequence, float theAvgStar, int theReviewCount)
    :sequence(theSequence), avgStar(theAvgStar), reviewCount(theReviewCount)
    {}
};

struct Business {
    int sequence;
    float avgStar;
    int reviewCount;
    float confident;
    
    Business(int theSequence, float theAvgStar, int theReviewCount)
    :sequence(theSequence), avgStar(theAvgStar), reviewCount(theReviewCount)
    {}
};


struct Review {
    string uid;
    string bid;
    float star;
    
    Review(string u, string b, float s)
    :uid(u), bid(b), star(s)
    {}
    
    bool operator < (const Review &other) const
    {
        if (uid < other.uid || (uid == other.uid && bid < other.bid)) {
            return true;
        }
        else
        {
            return false;
        }
    }
    
    bool operator == (const Review &other) const
    {
        return (uid == other.uid && bid == other.bid);
    }
};



map<string, float> result;  // 评分预测结果，key为uid与bid的连接

void loadTrainingSet(map<string, User> &userMap, map<string, Business> &businessMap, set<Review> &reviewSet, int &rowCount, int &colCount)
{
#ifdef LocalTest
    ifstream trainingReviewFile = ifstream("/Users/jtang1/Desktop/test2013/training_review.json");
#else
    ifstream trainingReviewFile = ifstream("/Users/jtang1/Desktop/2013/yelp_training_set/yelp_training_set_review.json");
#endif

    // 对于training_set_review根据uid和bid进行排序，便于直接生成稀疏矩阵
    if (trainingReviewFile.is_open()) {
        while (!trainingReviewFile.eof()) {
            string line;
            getline(trainingReviewFile, line);
            size_t start = line.find("\"user_id\":", 48);
            string uid = line.substr(start+12, 22);
            string bid = line.substr(line.length() - 24, 22);
            start = line.find("\"stars\"", 124);
            float stars = (float)atoi(line.substr(start+9, 1).c_str());
            
            map<string, User>::iterator userIter;
            if ((userIter = userMap.find(uid)) == userMap.end()) {
                userMap.insert(make_pair(uid, User(0, stars, 1)));
            }
            else
            {
                // 先计算总得打分，最后计算平均分，business类似
                ++(userIter->second.reviewCount);
                userIter->second.avgStar += stars;
            }
            
            map<string, Business>::iterator businessIter;
            if ((businessIter = businessMap.find(bid)) == businessMap.end()) {
                businessMap.insert(make_pair(bid, Business(0, stars, 1)));
            }
            else
            {
                ++(businessIter->second.reviewCount);
                businessIter->second.avgStar += stars;
            }
            
            reviewSet.insert(Review(uid, bid, stars));
        }
    }
    trainingReviewFile.close();
    
    
    // 根据用户ID的字符串顺序调整用户在矩阵中的位置
    // 计算用户打分的平均分
    int sequence = 0;
    for (map<string, User>::iterator iter = userMap.begin(); iter != userMap.end(); ++iter) {
        iter->second.avgStar /= iter->second.reviewCount;
        iter->second.sequence = sequence++;
    }
    rowCount = sequence;
    
    // 根据商家ID的字符串顺序调整商家在矩阵中的位置
    // 计算商家打分的平均分
    sequence = 0;
    for (map<string, Business>::iterator iter = businessMap.begin(); iter != businessMap.end(); ++iter) {
        iter->second.avgStar /= iter->second.reviewCount;
        iter->second.sequence = sequence++;
    }
    colCount = sequence;
    
    // load training_set_user to userMap
    ifstream trainingSetUserFile("/Users/jtang1/Desktop/2013/yelp_training_set/yelp_training_set_user.json");
    if (trainingSetUserFile.is_open()) {
        while (!trainingSetUserFile.eof()) {
            string line;
            getline(trainingSetUserFile, line);
            size_t start = line.find("\"user_id\":", 48);
            string uid = line.substr(start+12, 22);
            start = line.find("\"average_stars\"", 96);
            size_t end = line.find(",", start+17);
            float avg_stars = atof(line.substr(start+17, end - start - 17).c_str());
            start = line.find("\"review_count\"", end);
            end = line.find(",", start);
            int review_count = atoi(line.substr(start+16, end-start-16).c_str());
            map<string, User>::iterator userIter = userMap.find(uid);
            if (userIter != userMap.end()) {
                userIter->second.avgStar = avg_stars;
//                userIter->second.reviewCount = review_count;
            }
            else
            {
                userMap.insert(make_pair(uid, User(-1, avg_stars, review_count)));
            }
        }
    }
    // load training_set_business to businessMap
    ifstream trainingSetBusinessFile("/Users/jtang1/Desktop/2013/yelp_training_set/yelp_training_set_business.json");
    if (trainingSetBusinessFile.is_open()) {
        while (!trainingSetBusinessFile.eof()) {
            string line;
            getline(trainingSetBusinessFile, line);
            string bid = line.substr(17, 22);
            size_t start = line.find("\"stars\"");
            size_t end = line.find(",", start+9);
            float avg_stars = atof(line.substr(start+9, end - start - 9).c_str());
            start = line.find("\"review_count\"");
            end = line.find(",", start);
            int review_count = atoi(line.substr(start+16, end-start-16).c_str());
            map<string, Business>::iterator businessIter = businessMap.find(bid);
            if (businessIter != businessMap.end()) {
                businessIter->second.avgStar = avg_stars;
//                businessIter->second.reviewCount = review_count;
            }
            else
            {
                businessMap.insert(make_pair(bid, Business(-1, avg_stars, review_count)));
            }
        }
    }
}


void generateMatrix(SparseMatrix<float> &sparseM, const map<string, User> &userMap, const map<string, Business> &businessMap, const set<Review> &reviewSet)
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
        sparseM.data[index++] = {row, col, iter->star};
        lastRow = row;
    }
    sparseM.rpos[lastRow+1] = index;
}

template<class T, class V>
void PCC(const SparseMatrix<float> &sparseM,
         map<string, T> &rowMap,
         const map<string, V> &colMap,
         const multimap<string, string> &predictionMap)
{
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
                    nominator += tempa * tempu;
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
                if (sim > 0.01) {
                    simMap.insert(make_pair(userIter2->first, sim));
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
                nominator += (simIter->second * (uaStar - userIter->second.avgStar));
                denominator += simIter->second;
            }
            
            if (nominator >0 && denominator > 0) {
                float predictStar = rowMap.find(userIter1->first)->second.avgStar + (nominator/denominator);
                // 将计算结果插入到result中
                result.insert(make_pair(rangeIter->first+rangeIter->second, predictStar));
                ++resultInsertCount;
            }
        }
    }
    
    cout << "PCC: Result Insert Count:" << resultInsertCount << "\t" << "Result Count:" << result.size() << endl;
}


float computeRMSE(const string &predictionFileName)
{
    ifstream testReviewFile("/Users/jtang1/Desktop/test2013/test_review.json");
    ifstream predictionFile(predictionFileName);
    
    float sum = 0;
    int n = 0;
    if (testReviewFile.is_open() && predictionFile.is_open()) {
        while (!testReviewFile.eof() && !predictionFile.eof()) {
            string testLine, predictionLine;
            getline(testReviewFile, testLine);
            getline(predictionFile, predictionLine);
            
            size_t start = testLine.find("\"stars\"", 124);
            float testStar = (float)atoi(testLine.substr(start+9, 1).c_str());
            
            start = predictionLine.find(",");
            float predictionStar = atof(predictionLine.substr(start+1, predictionLine.length()-start-1).c_str());
            
            sum += (testStar - predictionStar) * (testStar - predictionStar);
            ++n;
        }
    }
    
    return sqrt(sum/n);
}

void UIPCC(const map<string, User> &userMap, const map<string, Business> &businessMap, float lamda)
{
#ifdef LocalTest
    ifstream submitionFile = ifstream("/Users/jtang1/Desktop/test2013/test_review.json");
#else
    ifstream submitionFile = ifstream("/Users/jtang1/Desktop/2013/sampleSubmission.csv");
#endif
    
    stringstream predictionFileName;
    
#ifdef LocalTest
    predictionFileName << "/Users/jtang1/Desktop/test2013/prediction/prediction2_lamda_" << lamda << ".csv";
#else
    predictionFileName << "/Users/jtang1/Desktop/2013/prediction/prediction2_lamda_" << lamda << ".csv";
#endif
    ofstream predictionFile = ofstream(predictionFileName.str());
    // 根据result和UserMap、BusinessMap中的评分平均值计算最终的评分
    if (submitionFile.is_open())
    {
#ifndef LocalTest
        predictionFile << "RecommendationId,Stars" << endl;
#endif
        string line;
        getline(submitionFile, line);
        int index = 0;
        
        int upccCount = 0;
        int ipccCount = 0;
        
        
        while (!submitionFile.eof())
        {
            getline(submitionFile, line);
#ifdef LocalTest
            size_t start;
            start = line.find("\"user_id\"");
            string uid = line.substr(start+12, 22);
            string bid = line.substr(line.length() - 24, 22);
#else
            string uid = line.substr(0, 22);
            string bid = line.substr(23, 22);
#endif
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
                    wu = lamda;
                    wi = 1-lamda;
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
            predictionFile << ++index << "," << prediction << endl;
        }
        
        cout << "UPCC Count: " << upccCount << "\tIPCC Count: " << ipccCount << endl;
    }
    submitionFile.close();
    predictionFile.close();
    
#ifdef LocalTest
    cout << "lamda=" << lamda << ": " << computeRMSE(predictionFileName.str()) << endl;
#endif
}


// 从训练集中分出一部分做测试集
void splitTrainingSet()
{
    ifstream reviewFile("/Users/jtang1/Desktop/test2013/review.json");
    ofstream trainingReviewFile("/Users/jtang1/Desktop/test2013/training_review.json");
    ofstream testReviewFile("/Users/jtang1/Desktop/test2013/test_review.json");
    
    int testLine = 0;
    int trainingLine = 0;
    if (reviewFile.is_open()) {
        while (!reviewFile.eof()) {
            string line;
            getline(reviewFile, line);
            srand((unsigned int)clock());
            if ((rand() % 100) < 10) {
                if (testLine != 0) {
                    testReviewFile << endl;
                }
                ++testLine;
                testReviewFile << line;
            }
            else
            {
                if (trainingLine != 0) {
                    trainingReviewFile << endl;
                }
                ++trainingLine;
                trainingReviewFile << line;
            }
        }
    }
    
    reviewFile.close();
    trainingReviewFile.close();
    testReviewFile.close();
}


int main(int argc, const char * argv[])
{
    map<string, User> userMap;
    map<string, Business> businessMap;
    set<Review> reviewSet;
    multimap<string, string> predictionUBMap;     // 需要预测的uid和bid
    multimap<string, string> predictionBUMap;     // 需要预测的bid和uid

    
    //analyzeDataSet();
#ifdef LocalTest
    ifstream submitionFile = ifstream("/Users/jtang1/Desktop/test2013/test_review.json");
#else
    ifstream submitionFile = ifstream("/Users/jtang1/Desktop/2013/sampleSubmission.csv");
#endif
    // 将需要预测的数据读入predictionMap
    if (submitionFile.is_open())
    {
        string line;
        getline(submitionFile, line);
        while (!submitionFile.eof())
        {
            getline(submitionFile, line);
#ifdef LocalTest
            size_t start;
            start = line.find("\"user_id\"");
            string uid = line.substr(start+12, 22);
            string bid = line.substr(line.length() - 24, 22);
#else
            string uid = line.substr(0, 22);
            string bid = line.substr(23, 22);
#endif
            predictionUBMap.insert(make_pair(uid, bid));
            predictionBUMap.insert(make_pair(bid, uid));
        }
    }
    submitionFile.close();
    
    
    int rowCount, colCount;
    loadTrainingSet(userMap, businessMap, reviewSet, rowCount, colCount);
    // 生成稀疏矩阵
    SparseMatrix<float> sparseUBMatrix(static_cast<int>(reviewSet.size()), rowCount, colCount);
    SparseMatrix<float> sparseBUMatrix(static_cast<int>(reviewSet.size()), colCount, rowCount);
    generateMatrix(sparseUBMatrix, userMap, businessMap, reviewSet);
    sparseUBMatrix.transposeMatrix(sparseBUMatrix);
    
    PCC(sparseUBMatrix, userMap, businessMap, predictionUBMap); // UPCC
    PCC(sparseBUMatrix, businessMap, userMap, predictionBUMap); // IPCC
    

    float lamda = 0.05;
    for (int i = 0; i < 19; ++i) {
        lamda += 0.05;
        UIPCC(userMap, businessMap, lamda);
    }

    
    
    return 0;
}

