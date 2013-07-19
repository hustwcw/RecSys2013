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


#include "BasicPMF.h"
#include "Util.h"
#include "SparseMatrix.h"

#define UserSize        (43873+3000)        // 45981
#define BusinessSize    (11537)
#define ReviewSize      229907
#define TOPK            200


using namespace std;




struct User {
    int sequence;
    float avgStar;
    int reviewCount;
    User(int theSequence, float theAvgStar, int theReviewCount)
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


int main(int argc, const char * argv[])
{
    //analyzeDataSet();
    ifstream trainingReviewFile = ifstream("/Users/jtang1/Desktop/2013/yelp_training_set/yelp_training_set_review.json");
    ifstream submitionFile = ifstream("/Users/jtang1/Desktop/2013/sampleSubmission.csv");
    map<string, User> userMap;
    map<string, int> businessMap;
    multimap<string, string> predictionMap;     // 需要预测的uid和bid
    map<string, float> result;  // 评分预测结果，key为uid与bid的连接
    
    // 将需要预测的数据读入predictionMap
    if (submitionFile.is_open()) {
        string line;
        getline(submitionFile, line);
        while (!submitionFile.eof()) {
            getline(submitionFile, line);
            string uid = line.substr(0, 22);
            string bid = line.substr(23, 22);
            predictionMap.insert(make_pair(uid, bid));
        }
    }
    
    // 对于training_set_review根据uid和bid进行排序，便于直接生成稀疏矩阵
    set<Review> reviewSet;
    if (trainingReviewFile.is_open()) {
        while (!trainingReviewFile.eof()) {
            string line;
            getline(trainingReviewFile, line);
            if (line.length() <= 0) {
                continue;
            }
            size_t start = line.find("\"user_id\":", 48);
            string uid = line.substr(start+12, 22);
            string bid = line.substr(line.length() - 24, 22);
            start = line.find("\"stars\"", 124);
            float stars = (float)atoi(line.substr(start+9, 1).c_str());
            
            map<string, User>::iterator iter;
            if ((iter = userMap.find(uid)) == userMap.end()) {
                userMap.insert(make_pair(uid, User(0, stars, 1)));
            }
            else
            {
                ++(iter->second.reviewCount);
                iter->second.avgStar = (iter->second.avgStar * (iter->second.reviewCount-1) + stars)/iter->second.reviewCount;
            }
            
            map<string, int>::iterator iter2;
            if ((iter2 = businessMap.find(bid)) == businessMap.end()) {
                businessMap.insert(make_pair(bid, 0));
            }
            else
            {
                ;
            }
            
            reviewSet.insert(Review(uid, bid, stars));
        }
    }
    
    // 根据用户ID的字符串顺序调整用户在矩阵中的位置
    int sequence = 0;
    for (map<string, User>::iterator iter = userMap.begin(); iter != userMap.end(); ++iter) {
        iter->second.sequence = sequence++;
    }
    // 根据商家ID的字符串顺序调整商家在矩阵中的位置
    sequence = 0;
    for (map<string, int>::iterator iter = businessMap.begin(); iter != businessMap.end(); ++iter) {
        iter->second = sequence++;
    }
    
    
    // 根据reviewSet生成稀疏矩阵
    SparseMatrix<float> sparseM(static_cast<int>(reviewSet.size()), static_cast<int>(userMap.size()), static_cast<int>(businessMap.size()));
    int index = 0;
    int lastRow = -1;
    for (set<Review>::iterator iter = reviewSet.begin(); iter != reviewSet.end(); ++iter) {
        int row = userMap.find(iter->uid)->second.sequence;
        if (row != lastRow) {
            sparseM.rpos[row] = index;
        }
        int col = businessMap.find(iter->bid)->second;
        sparseM.data[index++] = {row, col, iter->star};
        lastRow = row;
    }
    sparseM.rpos[lastRow+1] = index;
    
    // 对于每个用户，都计算他与其他用户的相似度
    int row = 0;
    for (map<string, User>::iterator userIter1 = userMap.begin(); userIter1 != userMap.end(); ++userIter1) {
        cout << ++row << ": ";
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
                    i++;
                    j++;
                }
                else if (sparseM.data[i].j < sparseM.data[j].j)
                {
                    i++;
                }
                else if (sparseM.data[i].j > sparseM.data[j].j)
                {
                    j++;
                }
            }
            
            if (nominator > 0 && denominator1 > 0 && denominator2 > 0) {
                float weight = (2.0 * intersectCount / (userIter1->second.reviewCount + userIter2->second.reviewCount));
                float sim = weight * (nominator/(sqrt(denominator1 * denominator2)));
//                cout << sim << endl;
                simMap.insert(make_pair(userIter2->first, sim));
            }
            // 对所有计算出来的用户的相似度进行排序，选取其中最大的K个相似度做该用户的评分预测
            // 需要预测的business可以从predictionMap中找到
        }
        cout << simMap.size() << endl;

        // 针对userIter1用户，找出所有需要预测的business
        pair<multimap<string, string>::iterator, multimap<string, string>::iterator> ret = predictionMap.equal_range(userIter1->first);
        for (multimap<string, string>::iterator rangeIter = ret.first; rangeIter != ret.second; ++rangeIter) {
            map<string, int>::iterator businessIter = businessMap.find(rangeIter->second);
            // 需要预测的business不在训练集中
            if (businessIter == businessMap.end()) {
                continue;
            }
            int col = businessIter->second;
            float nominator = 0;
            float denominator = 0;
            for (map<string, float>::iterator simIter = simMap.begin(); simIter != simMap.end(); ++simIter) {
                map<string, User>::iterator userIter = userMap.find(simIter->first);
                int row = userIter->second.sequence;
                float uaStar = sparseM.getElem(row, col);
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
                result.insert(make_pair(rangeIter->first+","+rangeIter->second, predictStar));
            }
        }
    }
    
    int temp = 0;
    for (map<string, float>::iterator iter = result.begin(); iter != result.end(); ++iter) {
        cout << temp++ << ": " << iter->first << "," << iter->second << endl;
    }

    
    return 0;
}

