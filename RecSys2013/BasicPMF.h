//
//  BasicPMF.h
//  RecSys2013
//
//  Created by JTangWang on 13-7-7.
//  Copyright (c) 2013年 JTangWang. All rights reserved.
//

#ifndef __RecSys2013__BasicPMF__
#define __RecSys2013__BasicPMF__

#include <iostream>
#include <map>
#include "Util.h"


template<class T>
class SparseMatrix;
class User;
class Business;


class BasicPMF {
    float lamda = 0.002;// 该参数影响不大
    float iterThreshold = 0.005;
    
    float( *matrixP)[UserSize];
    float (*matrixQ)[BusinessSize];
    int userCount;
    int businessCount;
    float learnRate;
    int factor;
    int iterCount;
public:
    BasicPMF(){}
    BasicPMF(int uCount, int bCount, float lrate, int f);
    
    
    void compute(const SparseMatrix<float> &starMatrix, const SparseMatrix<float> &transposeStarMatrix, const int maxIterCount, const std::map<std::string, User> &userMap, const std::map<std::string, Business> &businessMap);
    void predict(const std::map<std::string, User> &userMap, const std::map<std::string, Business> &businessMap, const std::map<std::string, Business> &testBusinessMap, const std::map<std::string, float> &cityAvgMap);
    ~BasicPMF()
    {
        delete [] matrixP;
        delete [] matrixQ;
    }
};

#endif /* defined(__RecSys2013__BasicPMF__) */
