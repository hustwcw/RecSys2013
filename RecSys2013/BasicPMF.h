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

struct SparseMatrix;

class BasicPMF {
    float lamda = 0.001;
    float sparsity = 0.05;
    int latentDim = 10;
    float learnRate = 0.0045;
    int maxIterNum = 24;
    float iterThreshold = 0.005;
    
    float( *matrixP)[UserSize];
    float (*matrixQ)[BusinessSize];
    
public:
    BasicPMF(){}
    BasicPMF(int userCount, int businessCount, int Factor);
    
    
    void compute(const SparseMatrix &starMatrix);
    ~BasicPMF()
    {
        delete [] matrixP;
        delete [] matrixQ;
    }
};

#endif /* defined(__RecSys2013__BasicPMF__) */
