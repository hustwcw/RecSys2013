//
//  Util.cpp
//  RecSys2013
//
//  Created by JTangWang on 13-7-16.
//  Copyright (c) 2013年 JTangWang. All rights reserved.
//

#include "Util.h"

#include <set>
#include <map>
#include <vector>
#include <string>
#include <fstream>
#include <cmath>
#include <algorithm>

#include <boost/algorithm/string/classification.hpp>
#include <boost/algorithm/string/split.hpp>
#include <boost/lexical_cast.hpp>


#include "User.h"
#include "Category.h"


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


float computeRMSE(const string &predictionFileName)
{
    ifstream testReviewFile("/Users/jtang1/Desktop/test2013/sampleSubmission.csv");
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
            
            sum += ((testStar - predictionStar) * (testStar - predictionStar));
            ++n;
        }
    }
    
    return sqrt(sum/n);
}


void loadDataToPredict(multimap<string, string> &predictionUBMap, multimap<string, string> &predictionBUMap)
{
    ifstream submitionFile = ifstream(FolderName + "sampleSubmission.csv");
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
    else
    {
        cout << "can't open submitionFile" << endl;
    }
    submitionFile.close();
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


void loadGenderFile(map<string, bool> &genderMap)
{
    ifstream genderFile("/Users/jtang1/Desktop/2013/mf.txt");
    if (genderFile.is_open()) {
        string line;
        while (!genderFile.eof()) {
            getline(genderFile, line);
            size_t pos = line.find("\t");
            string name = line.substr(0, pos);
            string gender = line.substr(line.length()-2, 1);
            if (gender == string("m")) {
                genderMap.insert(make_pair(name, true));
            }
            else
            {
                genderMap.insert(make_pair(name, false));
            }
        }
    }
    else
    {
        cout << "can't open genderFile" << endl;
    }
}


void loadCategory(map<string, Category> &categoryMap)
{
    ifstream categoryFile(FolderName + "CateAvgStar.txt");
    if (categoryFile.is_open()) {
        string line;
        getline(categoryFile, line);
        while (!categoryFile.eof()) {
            getline(categoryFile, line);
            vector<string> vStr;
            boost::split( vStr, line, boost::is_any_of( ":" ), boost::token_compress_on );
            categoryMap.insert(make_pair(vStr[0], Category(boost::lexical_cast<float>(vStr[1]), boost::lexical_cast<int>(vStr[2]), boost::lexical_cast<int>(vStr[3]), boost::lexical_cast<float>(vStr[4]))));
        }
    }
    else
    {
        cout << "Can't open category file" << endl;
    }
}

void analyzeGenderDistribution(const map<string, bool> &genderMap, const map<string, User> &userMap)
{
    ifstream trainingReviewFile("/Users/jtang1/Desktop/2013/yelp_training_set/yelp_training_set_review.json");
    vector<int> maleStarVec;
    vector<int> femaleStarVec;
    if (trainingReviewFile.is_open()) {
        string line;
        while (!trainingReviewFile.eof()) {
            getline(trainingReviewFile, line);
            size_t start = line.find("\"user_id\":", 48);
            string uid = line.substr(start+12, 22);
            start = line.find("\"stars\"", 124);
            int stars = atoi(line.substr(start+9, 1).c_str());
            // 根据uid判断性别
            string name = userMap.find(uid)->second.name;
            transform(name.begin(), name.end(), name.begin(), ::toupper);
            map<string, bool>::const_iterator iter = genderMap.find(name);
            if (iter != genderMap.end()) {
                bool gender = iter->second;
                if (gender == true) {
                    // 男性
                    maleStarVec.push_back(stars);
                }
                else
                {
                    femaleStarVec.push_back(stars);
                }
            }
            else
            {
                cout << "can't find the name:\t" << name << endl;
            }
            
        }
    }
    else
    {
        cout << "can't open trainingReviewFile" << endl;
    }
    
    cout << "Male\tFemale\tGlobal" << endl;
    // 对男女用户的评分数据进行分析，计算平均值和方差
    float maleAvg, femaleAvg, globalAvg;
    float maleRMSE, femaleRMSE, globalRMSE;
    float maleSum = 0, femaleSum = 0;
    for (vector<int>::iterator iter = maleStarVec.begin(); iter != maleStarVec.end(); ++iter) {
        maleSum += *iter;
    }
    for (vector<int>::iterator iter = femaleStarVec.begin(); iter != femaleStarVec.end(); ++iter) {
        femaleSum += *iter;
    }
    cout << maleStarVec.size() << "\t" << femaleStarVec.size() << "\t" << maleStarVec.size()+femaleStarVec.size() << endl;
    maleAvg = maleSum / maleStarVec.size();
    femaleAvg = femaleSum / femaleStarVec.size();
    globalAvg = (maleSum + femaleSum) / (maleStarVec.size() + femaleStarVec.size());
    cout << maleAvg << "\t" << femaleAvg << "\t" << globalAvg << endl;
    
    float maleRMSESum = 0, femaleRMSESum = 0, globalRMSESum = 0;
    for (vector<int>::iterator iter = maleStarVec.begin(); iter != maleStarVec.end(); ++iter) {
        maleRMSESum += (*iter - maleAvg)*(*iter - maleAvg);
        globalRMSESum += (*iter - globalAvg) * (*iter - globalAvg);
    }
    for (vector<int>::iterator iter = femaleStarVec.begin(); iter != femaleStarVec.end(); ++iter) {
        femaleRMSESum += (*iter - femaleAvg)*(*iter - femaleAvg);
        globalRMSESum += (*iter - globalAvg) * (*iter - globalAvg);
    }
    maleRMSE = sqrt(maleRMSESum / maleStarVec.size());
    femaleRMSE = sqrt(femaleRMSESum/ femaleStarVec.size());
    globalRMSE = sqrt(globalRMSESum / (maleStarVec.size() + femaleStarVec.size()));
    cout << maleRMSE << "\t" << femaleRMSE << "\t" << globalRMSE << endl;
}









