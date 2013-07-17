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


#define UserSize        (43873+3000)        // 45981
#define BusinessSize    (11537)

using namespace std;


float rateMatrix[UserSize][BusinessSize];

struct User {
    int sequence;
    float avgStar;
    int reviewCount;
    User(int theSequence, float theAvgStar, int theReviewCount)
    :sequence(theSequence), avgStar(theAvgStar), reviewCount(theReviewCount)
    {}
};
int main(int argc, const char * argv[])
{
    //analyzeDataSet();
    ifstream trainingReviewFile = ifstream("/Users/jtang1/Desktop/2013/yelp_training_set/yelp_training_set_review.json");
    ifstream submitionFile = ifstream("/Users/jtang1/Desktop/2013/sampleSubmission.csv");
    map<string, User> userMap;
    map<string, int> businessMap;
    multimap<string, string> predictionMap;

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
    
    // initialize the matrix with 0.0
    for (int i = 0; i < UserSize; i++) {
        for (int j = 0; j < BusinessSize; j++) {
            rateMatrix[i][j] = 0.0;
        }
    }
    int userCount = 0;
    int businessCount = 0;
    if (trainingReviewFile.is_open()) {
        while (!trainingReviewFile.eof()) {
            string line;
            int row, col;
            getline(trainingReviewFile, line);
            if (line.length() <= 0) {
                continue;
            }
            size_t start = line.find("\"user_id\": \"");
            string uid;
            uid = line.substr(start+12, 22);
            start = line.find("\"business_id\": \"");
            string bid = line.substr(start+16, 22);
            start = line.find("\"stars\"");
            float stars = (float)atoi(line.substr(start+9, 1).c_str());
            
            map<string, User>::iterator iter;
            if ((iter = userMap.find(uid)) == userMap.end()) {
                row = userCount;
                userMap.insert(make_pair(uid, User(userCount, stars, 1)));
                userCount++;
            }
            else
            {
                row = iter->second.sequence;
                iter->second.reviewCount++;
                iter->second.avgStar = (iter->second.avgStar * (iter->second.reviewCount-1) + stars)/iter->second.reviewCount;
            }
            
            map<string, int>::iterator iter2;
            if ((iter2 = businessMap.find(bid)) == businessMap.end()) {
                col = businessCount;
                businessMap.insert(make_pair(bid, businessCount++));
            }
            else
            {
                col = iter2->second;
            }
            
            rateMatrix[row][col] = stars;
        }
    }
    
    // 
    // 对于每个用户，都计算他与其他用户的相似度
    int row = 0;
    for (map<string, User>::iterator iter1 = userMap.begin(); iter1 != userMap.end(); ++iter1) {
        cout << ++row << endl;
        for (map<string, User>::iterator iter2 = iter1; iter2 != userMap.end(); ++iter2) {
            if (iter2 == iter1) {
                continue;
            }
            // 计算iter1和iter2的相似度
            float nominator = 0.0f;
            float denominator1 = 0.0f;
            float denominator2 = 0.0f;
            int intersectCount = 0;
            int row1 = iter1->second.sequence;
            int row2 = iter2->second.sequence;
            for (int i = 0; i < BusinessSize; ++i) {
                if (rateMatrix[row1][i]!=0 && rateMatrix[row2][i]!=0) {
                    float tempa = rateMatrix[row1][i] - iter1->second.avgStar;
                    float tempu = rateMatrix[row2][i] - iter2->second.avgStar;
                    nominator += tempa * tempu;
                    denominator1 += tempa * tempa;
                    denominator2 += tempu * tempu;
                    intersectCount++;
                }
            }
            if (nominator > 0 && denominator1 > 0 && denominator2 > 0) {
                float weight = (2.0 * intersectCount / (iter1->second.reviewCount + iter2->second.reviewCount));
                float sim = weight * (nominator/(sqrt(denominator1) * sqrt(denominator2)));
//                cout << sim << endl;
            }
        }
    }
    cout << "user count: " << userCount << "\nbusiness count: " << businessCount << endl;
    
    return 0;
}

