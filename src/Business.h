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
    float cateAvgStar;  // 通过类别信息计算出来的平均分
    int reviewCount;
    float confident;
    std::string city;
    
    Business(){}
    
    Business(int theSequence, float theAvgStar, float theCateAvgStar, int theReviewCount, const std::string &theCity)
    :sequence(theSequence), avgStar(theAvgStar), cateAvgStar(theCateAvgStar), reviewCount(theReviewCount), city(theCity)
    {}
};


#endif /* defined(__RecSys2013__Business__) */
