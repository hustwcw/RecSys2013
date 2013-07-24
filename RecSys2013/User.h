//
//  User.h
//  RecSys2013
//
//  Created by JTangWang on 13-7-24.
//  Copyright (c) 2013年 JTangWang. All rights reserved.
//

#ifndef __RecSys2013__User__
#define __RecSys2013__User__

#include <iostream>


struct User {
    int sequence;
    float avgStar;
    int reviewCount;
    float confident;
    
    User(){}
    
    User(int theSequence, float theAvgStar, int theReviewCount)
    :sequence(theSequence), avgStar(theAvgStar), reviewCount(theReviewCount)
    {}
};


#endif /* defined(__RecSys2013__User__) */
