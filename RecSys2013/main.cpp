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

#include "BasicPMF.h"
#include "Util.h"


#define UserSize        43873
#define BusinessSize    11537

using namespace std;





int main(int argc, const char * argv[])
{
    //analyzeDataSet();
    float rateMatrix[UserSize][BusinessSize];
    string trainingReviewFileName = "/Users/jtang1/Desktop/2013/yelp_training_set/yelp_training_set_review.json";
    ifstream trainingReviewFile = ifstream(trainingReviewFileName);
    map<string, int> userMap, businessMap;
    
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
            
            map<string, int>::iterator iter;
            if ((iter = userMap.find(uid)) == userMap.end()) {
                row = userCount;
                userMap.insert(make_pair(uid, userCount++));
            }
            else
            {
                row = iter->second;
            }
            if ((iter = businessMap.find(bid)) == businessMap.end()) {
                col = businessCount;
                businessMap.insert(make_pair(bid, businessCount++));
            }
            else
            {
                col = iter->second;
            }
            
            rateMatrix[row][col] = stars;
            
        }
    }
    
    cout << "user count: " << userCount << "\tbusiness count: " << businessCount << endl;
    
    return 0;
}

