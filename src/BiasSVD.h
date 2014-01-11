//
//  BiasSVD.h
//  RecSys2013
//
//  Created by JTangWang on 13-7-7.
//  Copyright (c) 2013年 JTangWang. All rights reserved.
//

#ifndef __RecSys2013__BiasSVD__
#define __RecSys2013__BiasSVD__

#include <iostream>
#include <map>
#include "Util.h"

template<class T>
class SparseMatrix;
class User;
class Business;


class BiasSVD {
    float lamda;
    
    float( *matrixP)[UserSize];
    float (*matrixQ)[BusinessSize];
    float ubias[UserSize];
    float bbias[BusinessSize];
    int userCount;
    int businessCount;
    float learnRate;
    int factor;
    int iterCount;
public:
    BiasSVD(int uCount, int bCount, float learnRate, int f);
    
    
    void compute(const SparseMatrix<std::pair<float, int> > &starMatrix,
                 const SparseMatrix<std::pair<float, int> > &transposeStarMatrix,
                 const int maxIterCount);
    
    void predict(const std::map<std::string, User> &userMap, const std::map<std::string, Business> &businessMap, const std::map<std::string, Business> &testBusinessMap);
    
    ~BiasSVD()
    {
        delete [] matrixP;
        delete [] matrixQ;
    }
};


#endif /* defined(__RecSys2013__BiasSVD__) */
