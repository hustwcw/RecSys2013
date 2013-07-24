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



#define UserSize        (1024*48)
#define BusinessSize    (1024*12)
template<class T>
class SparseMatrix;

class BasicPMF {
    float lamda = 0.001;
    float learnRate = 0.0005;
    int maxIterNum = 100;
    float iterThreshold = 0.005;
    
    float( *matrixP)[UserSize];
    float (*matrixQ)[BusinessSize];
    int userCount;
    int businessCount;
    int factor;
    
public:
    BasicPMF(){}
    BasicPMF(int uCount, int bCount, int f);
    
    
    void compute(const SparseMatrix<float> &starMatrix, const SparseMatrix<float> &transposeStarMatrix);
    ~BasicPMF()
    {
        delete [] matrixP;
        delete [] matrixQ;
    }
};

#endif /* defined(__RecSys2013__BasicPMF__) */
