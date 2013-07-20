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


ifstream trainingReviewFile = ifstream("/Users/jtang1/Desktop/2013/yelp_training_set/yelp_training_set_review.json");
ifstream submitionFile1 = ifstream("/Users/jtang1/Desktop/2013/sampleSubmission.csv");
ifstream submitionFile2 = ifstream("/Users/jtang1/Desktop/2013/sampleSubmission.csv");
ofstream predictionFile = ofstream("/Users/jtang1/Desktop/2013/prediction.csv");
multimap<string, string> predictionMap;     // 需要预测的uid和bid
map<string, float> result;  // 评分预测结果，key为uid与bid的连接
map<string, User> userMap;
map<string, Business> businessMap;
SparseMatrix<float> *sparseM;
float lamda = 0.4;

void generateMatrix()
{
    // 对于training_set_review根据uid和bid进行排序，便于直接生成稀疏矩阵
    set<Review> reviewSet;
    if (trainingReviewFile.is_open()) {
        while (!trainingReviewFile.eof()) {
            string line;
            getline(trainingReviewFile, line);
            size_t start = line.find("\"user_id\":", 48);
            string uid = line.substr(start+12, 22);
            string bid = line.substr(line.length() - 24, 22);
            start = line.find("\"stars\"", 124);
            float stars = (float)atoi(line.substr(start+9, 1).c_str());
            
            // TODO:reviewCount和avgStar换成user和business文件中给出的数据试试比较结果
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
    
    // 根据用户ID的字符串顺序调整用户在矩阵中的位置
    // 计算用户打分的平均分
    int sequence = 0;
    for (map<string, User>::iterator iter = userMap.begin(); iter != userMap.end(); ++iter) {
        iter->second.avgStar /= iter->second.reviewCount;
        iter->second.sequence = sequence++;
    }
    // 根据商家ID的字符串顺序调整商家在矩阵中的位置
    // 计算商家打分的平均分
    sequence = 0;
    for (map<string, Business>::iterator iter = businessMap.begin(); iter != businessMap.end(); ++iter) {
        iter->second.avgStar /= iter->second.reviewCount;
        iter->second.sequence = sequence++;
    }
    
    // 根据reviewSet生成稀疏矩阵
    sparseM = new SparseMatrix<float>(static_cast<int>(reviewSet.size()), static_cast<int>(userMap.size()), static_cast<int>(businessMap.size()));
    int index = 0;
    int lastRow = -1;
    for (set<Review>::iterator iter = reviewSet.begin(); iter != reviewSet.end(); ++iter) {
        int row = userMap.find(iter->uid)->second.sequence;
        if (row != lastRow) {
            sparseM->rpos[row] = index;
        }
        int col = businessMap.find(iter->bid)->second.sequence;
        sparseM->data[index++] = {row, col, iter->star};
        lastRow = row;
    }
    sparseM->rpos[lastRow+1] = index;
}


void UPCC()
{
    // 对于每个用户，都计算他与其他用户的相似度
    int row = 0;
    int resultInsertCount = 0;
    for (map<string, User>::iterator userIter1 = userMap.begin(); userIter1 != userMap.end(); ++userIter1) {
        if ((++row)%1024 == 0) {
            cout << row << endl;
        }
        // 每个用户都有一个相似度map，记录该用户与其他用户之间的相似度
        map<string, float> simMap; // uid-sim Map
        for (map<string, User>::iterator userIter2 = userMap.begin(); userIter2 != userMap.end(); ++userIter2) {
            if (userIter2 == userIter1) {
                continue;
            }
            // 计算iter1和iter2的相似度
            float nominator = 0.0f;
            float denominator1 = 0.0f;
            float denominator2 = 0.0f;
            int intersectCount = 0;
            int row1 = userIter1->second.sequence;
            int row2 = userIter2->second.sequence;
            int i = sparseM->rpos[row1];
            int j = sparseM->rpos[row2];
            for (; i < sparseM->rpos[row1+1] && j < sparseM->rpos[row2+1]; ) {
                if (sparseM->data[i].j == sparseM->data[j].j) {
                    // 列号相同，计算相似度
                    float tempa = sparseM->data[i].elem - userIter1->second.avgStar;
                    float tempu = sparseM->data[j].elem - userIter2->second.avgStar;
                    nominator += tempa * tempu;
                    denominator1 += tempa * tempa;
                    denominator2 += tempu * tempu;
                    intersectCount++;
                    ++i;
                    ++j;
                }
                else if (sparseM->data[i].j < sparseM->data[j].j)
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
                simMap.insert(make_pair(userIter2->first, sim));
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
        pair<multimap<string, string>::iterator, multimap<string, string>::iterator> ret = predictionMap.equal_range(userIter1->first);
        for (multimap<string, string>::iterator rangeIter = ret.first; rangeIter != ret.second; ++rangeIter) {
            map<string, Business>::iterator businessIter = businessMap.find(rangeIter->second);
            // 需要预测的business不在训练集中
            if (businessIter == businessMap.end()) {
                continue;
            }
            int col = businessIter->second.sequence;
            float nominator = 0;
            float denominator = 0;
            for (map<string, float>::iterator simIter = simMap.begin(); simIter != simMap.end(); ++simIter) {
                map<string, User>::iterator userIter = userMap.find(simIter->first);
                int row = userIter->second.sequence;
                float uaStar = sparseM->getElem(row, col);
                // 有相似度的用户对于该要预测的商家没有过review
                if (uaStar <= 0) {
                    continue;
                }
                nominator += (simIter->second * (uaStar - userIter->second.avgStar));
                denominator += simIter->second;
            }
            
            if (nominator > 0 && denominator > 0) {
                float predictStar = userMap.find(rangeIter->first)->second.avgStar + (nominator/denominator);
                // 将计算结果插入到result中
                result.insert(make_pair(rangeIter->first+rangeIter->second, predictStar));
                ++resultInsertCount;
            }
        }
    }
    
    cout << "UPCC: Result Insert Count:" << resultInsertCount << "\t" << "Result Count:" << result.size() << endl;
}


void IPCC()
{
    // 将User-Business稀疏矩阵sparseM转置为Business-User矩阵
    SparseMatrix<float> BUMatrix(sparseM->tu, sparseM->nu, sparseM->mu);
    int q = 0;
    for (int col = 0; col < sparseM->nu; ++col) {
        BUMatrix.rpos[col] = q;
        for (int p = 0; p < sparseM->tu; ++p) {
            if (sparseM->data[p].j == col) {
                BUMatrix.data[q].i = sparseM->data[p].j;
                BUMatrix.data[q].j = sparseM->data[p].i;
                BUMatrix.data[q].elem = sparseM->data[p].elem;
                ++q;
            }
        }
    }
    BUMatrix.rpos[sparseM->nu] = q;
    
    // 对于每个商家，都计算他与其他商家的相似度
    int row = 0;
    int resultInsertCount = 0;
    for (map<string, Business>::iterator businessIter1 = businessMap.begin(); businessIter1 != businessMap.end(); ++businessIter1)
    {
        if ((++row)%1024 == 0) {
            cout << row << endl;
        }
        map<string, float> simMap; // bid-sim Map
        for (map<string, Business>::iterator businessIter2 = businessMap.begin(); businessIter2 != businessMap.end(); ++businessIter2)
        {
            if (businessIter2 == businessIter1) {
                continue;
            }
            // 计算iter1和iter2的相似度
            float nominator = 0.0f;
            float denominator1 = 0.0f;
            float denominator2 = 0.0f;
            int intersectCount = 0;
            int row1 = businessIter1->second.sequence;
            int row2 = businessIter2->second.sequence;
            int i = BUMatrix.rpos[row1];
            int j = BUMatrix.rpos[row2];
            for (; i < BUMatrix.rpos[row1+1] && j < BUMatrix.rpos[row2+1]; ) {
                if (BUMatrix.data[i].j == BUMatrix.data[j].j)
                {
                    // 列号相同，计算相似度
                    float tempa = BUMatrix.data[i].elem - businessIter1->second.avgStar;
                    float tempu = BUMatrix.data[j].elem - businessIter2->second.avgStar;
                    nominator += tempa * tempu;
                    denominator1 += tempa * tempa;
                    denominator2 += tempu * tempu;
                    ++intersectCount;
                    ++i;
                    ++j;
                }
                else if (BUMatrix.data[i].j < BUMatrix.data[j].j)
                {
                    ++i;
                }
                else// if (BUMatrix.data[i].j > BUMatrix.data[j].j)
                {
                    ++j;
                }
            }
            
            if (nominator > 0 && denominator1 > 0 && denominator2 > 0) {
                float weight = (2.0 * intersectCount / (businessIter1->second.reviewCount + businessIter2->second.reviewCount));
                float sim = weight * (nominator / sqrt(denominator1 * denominator2));
                simMap.insert(make_pair(businessIter2->first, sim));
            }
        }
        // 根据simMap计算Business的confident weight
        float simSum = 0;
        float nominator = 0;
        for (map<string, float>::iterator iter = simMap.begin(); iter != simMap.end(); ++iter) {
            simSum += iter->second;
        }
        for (map<string, float>::iterator iter = simMap.begin(); iter != simMap.end(); ++iter) {
            nominator += (iter->second) * (iter->second);
        }
        if (nominator >0 && simSum > 0) {
            businessIter1->second.confident = nominator / simSum;
        }
        else
        {
            businessIter1->second.confident = 0;
        }
        
        
        // 针对businessIter1用户，找出所有需要预测的user
        for (multimap<string, string>::iterator predictionIter = predictionMap.begin(); predictionIter != predictionMap.end(); ++predictionIter)
        {
            if (predictionIter->second != businessIter1->first) {
                continue;
            }
            map<string, User>::iterator userIter = userMap.find(predictionIter->first);
            // 需要预测的user不在训练集中
            if (userIter == userMap.end()) {
                continue;
            }
            int col = userIter->second.sequence;
            float nominator = 0;
            float denominator = 0;
            for (map<string, float>::iterator simIter = simMap.begin(); simIter != simMap.end(); ++simIter)
            {
                map<string, Business>::iterator businessIter = businessMap.find(simIter->first);
                int row = businessIter->second.sequence;
                float uaStar = BUMatrix.getElem(row, col);
                // 有相似度的商家对于该要预测的用户没有过review
                if (uaStar <= 0) {
                    continue;
                }
                nominator += (simIter->second * (uaStar - businessIter->second.avgStar));
                denominator += simIter->second;
            }
            
            if (nominator > 0 && denominator > 0) {
                float predictStar = businessMap.find(predictionIter->second)->second.avgStar + (nominator/denominator);
                // 将计算结果插入到result中
                result.insert(make_pair(predictionIter->second+predictionIter->first, predictStar));
                ++resultInsertCount;
            }
        }
    }
    
    cout << "UPCC: Result Insert Count:" << resultInsertCount << "\t" << "Result Count:" << result.size() << endl;
}


int main(int argc, const char * argv[])
{
    //analyzeDataSet();
    // 将需要预测的数据读入predictionMap
    if (submitionFile1.is_open())
    {
        string line;
        getline(submitionFile1, line);
        while (!submitionFile1.eof())
        {
            getline(submitionFile1, line);
            string uid = line.substr(0, 22);
            string bid = line.substr(23, 22);
            predictionMap.insert(make_pair(uid, bid));
        }
    }
    submitionFile1.close();
    // 生成稀疏矩阵
    generateMatrix();
    UPCC();
    IPCC();
    delete sparseM;
    
//    int temp = 0;
//    for (map<string, float>::iterator iter = result.begin(); iter != result.end(); ++iter) {
//        cout << temp++ << ": " << iter->first << "," << iter->second << endl;
//    }
    // 根据result和UserMap、BusinessMap中的评分平均值计算最终的评分
    if (submitionFile2.is_open())
    {
        predictionFile << "RecommendationId,Stars" << endl;
        string line;
        getline(submitionFile2, line);
        int index = 0;
        
        int upccCount = 0;
        int ipccCount = 0;
        
        
        while (!submitionFile2.eof())
        {
            getline(submitionFile2, line);
            string uid = line.substr(0, 22);
            string bid = line.substr(23, 22);
            map<string, User>::iterator userIter = userMap.find(uid);
            map<string, Business>::iterator businessIter = businessMap.find(bid);
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
                ipcc = userResultIter->second;
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
                prediction = userIter->second.avgStar;
            }
            else if (businessInTraining)
            {
                // 只有商家在训练集中
                prediction = businessIter->second.avgStar;
            }
            else
            {
                // 用户和商家都不在训练集中
                prediction = 3.67452543988905;
            }
            if (prediction > 5) {
                prediction = 5;
            }
            stringstream out;
            out << ++index << "," << prediction << endl;
            predictionFile << out.str();
        }
        
        cout << "UPCC Count: " << upccCount << "\tIPCC Count: " << ipccCount << endl;
    }
    submitionFile2.close();
    
    
    return 0;
}

