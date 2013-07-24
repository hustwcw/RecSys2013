//
//  Business.h
//  RecSys2013
//
//  Created by JTangWang on 13-7-24.
//  Copyright (c) 2013年 JTangWang. All rights reserved.
//

#ifndef __RecSys2013__Business__
#define __RecSys2013__Business__

#include <iostream>


struct Business {
    int sequence;
    float avgStar;
    int reviewCount;
    float confident;
    
    Business(){}
    
    Business(int theSequence, float theAvgStar, int theReviewCount)
    :sequence(theSequence), avgStar(theAvgStar), reviewCount(theReviewCount)
    {}
};


#endif /* defined(__RecSys2013__Business__) */
