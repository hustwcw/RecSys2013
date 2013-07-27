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

#define UserSize        (1024*48)
#define BusinessSize    (1024*12)
#define GlobalAvg       (3.766723)


//#define LocalTest


#define INIT_VARIANCE   0.05          // variance range from the INIT_SEED value
#define SmallRandom ((2.0*(rand()/(float)(RAND_MAX)) - 1.0)*INIT_VARIANCE) // meaning: rand[-INIT_VARIANCE, +INIT_VARIANCE]


#ifdef LocalTest
    #define FolderName string("/Users/jtang1/Desktop/test2013/")
#else
    #define FolderName string("/Users/jtang1/Desktop/2013/")
#endif


// 对数据集的特征进行简单的分析
void analyzeDataSet();
float computeRMSE(const std::string &predictionFileName);
void loadDataToPredict(std::map<std::string, std::string> &predictionMap, std::map<std::string, std::string> &transposePredictionMap);

#endif /* defined(__RecSys2013__Util__) */
