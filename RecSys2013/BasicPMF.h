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

class BasicPMF {
    float lamda = 0.001;
    float sparsity = 0.05;
    int latentDim = 10;
    float learnRate = 0.0045;
    int maxIterNum = 24;
    float iterThreshold = 0.005;
    
    
public:
    BasicPMF()
    {
        
    }
};

#endif /* defined(__RecSys2013__BasicPMF__) */
