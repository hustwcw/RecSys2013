//
//  Util.h
//  RecSys2013
//
//  Created by JTangWang on 13-7-16.
//  Copyright (c) 2013年 JTangWang. All rights reserved.
//

#ifndef __RecSys2013__Util__
#define __RecSys2013__Util__

#include <iostream>
#include <map>
#include <vector>



#define UserSize        (1024*48)
#define BusinessSize    (1024*12)
#define GlobalAvg       (3.766723)
#define GlobalRMSE      (1.217)

//#define LocalTest


#define INIT_VARIANCE   0.0005          // variance range from the INIT_SEED value
#define SmallRandom ((2.0*(rand()/(float)(RAND_MAX)) - 1.0)*INIT_VARIANCE) // meaning: rand[-INIT_VARIANCE, +INIT_VARIANCE]

#define DataPath    string("/Users/jtang1/Documents/Github/RecSys2013/Data/")
#define StatisticsPath (DataPath + "Statistics/")
#ifdef LocalTest
    #define FolderName (DataPath + "test2013/")
#else
    #define FolderName (DataPath + "2013/")
#endif


class User;
class Business;
class Category;


float calculateVectorAvg(const std::vector<float> &vec);
float calculateVectorRMSE(const std::vector<float> &vec, float avg);


// 对数据集的特征进行简单的分析
void analyzeDataSet();
float computeRMSE(const std::string &predictionFileName);
void loadDataToPredict(std::multimap<std::string, std::string> &predictionMap, std::multimap<std::string, std::string> &transposePredictionMap);

void splitTrainingSet();

void loadGenderFile(std::map<std::string, bool> &genderMap);

void loadCategory(std::map<std::string, Category> &categoryMap);

void loadCityAvg(std::map<std::string, float> &cityAvgMap);

void loadTestBusiness(std::map<std::string, Business> &testBusinessMap, const std::map<std::string, Category> &categoryMap);







float calculateUserAvgLatestReviewRMSE(const std::map<std::string, User> &userMap);




void analyzeGenderDistribution(const std::map<std::string, bool> &genderMap, const std::map<std::string, User> &userMap);

void deleteTextForReview();

void latestReviewForBusiness();
void latestReviewForUser();


#endif /* defined(__RecSys2013__Util__) */
