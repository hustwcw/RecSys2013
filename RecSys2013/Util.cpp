//
//  Util.cpp
//  RecSys2013
//
//  Created by JTangWang on 13-7-16.
//  Copyright (c) 2013年 JTangWang. All rights reserved.
//

#include "Util.h"

#include <set>
#include <string>
#include <fstream>


using namespace std;

void analyzeDataSet()
{
    set<string> trainingUserSet, testUserSet, testInTrainingUserSet;
    set<string> trainingBusiSet, testBusiSet, testInTrainingBusiSet;
    
    string trainingUserFileName = "/Users/jtang1/Desktop/2013/yelp_training_set/yelp_training_set_review.json";
    string testUserFileName = "/Users/jtang1/Desktop/2013/yelp_test_set/yelp_test_set_review.json";
    string line;
    ifstream finTraining = ifstream(trainingUserFileName);
    ifstream finTest = ifstream(testUserFileName);
    if (finTraining.is_open()) {
        while (!finTraining.eof()) {
            getline(finTraining, line);
            if (line.length() <= 0) {
                continue;
            }
            size_t start = line.find("\"user_id\": \"");
            string uid;
            uid = line.substr(start+12, 22);
            start = line.find("\"business_id\": \"");
            string bid = line.substr(start+16, 22);
            
            trainingUserSet.insert(uid);
            trainingBusiSet.insert(bid);
        }
    }
    
    cout << "training user count: " << trainingUserSet.size() << endl;
    cout << "training busi count: " << trainingBusiSet.size() << endl;
    finTraining.close();
    
    int UBcount = 0;
    int Ucount = 0;
    int Bcount = 0;
    int NUBcount = 0;
    if (finTest.is_open()) {
        while (!finTest.eof()) {
            getline(finTest, line);
            if (line.length() <= 0) {
                continue;
            }
            
            size_t start = line.find("\"user_id\": \"");
            string uid = line.substr(start+12, 22);
            start = line.find("\"business_id\": \"");
            string bid = line.substr(start+16, 22);
            bool userIn = trainingUserSet.find(uid) != trainingUserSet.end();
            bool busiIn = trainingBusiSet.find(bid) != trainingBusiSet.end();
            if (userIn && busiIn) {
                UBcount++;
            }
            else if (userIn)
            {
                Ucount++;
            }
            else if (busiIn)
            {
                Bcount++;
            }
            else
            {
                NUBcount++;
            }
        }
    }
    finTest.close();
    
    cout << "U&B: " << UBcount << endl;
    cout << "Only U: " << Ucount << endl;
    cout << "Only B: " << Bcount << endl;
    cout << "None: " << NUBcount << endl;
    cout << "All: " << UBcount+Ucount+Bcount+NUBcount << endl;
}