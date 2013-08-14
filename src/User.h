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
#include <vector>

struct User {
    int sequence;
    float avgStar;
    int reviewCount;
    float confident;
    std::string name;
    std::vector<float> starVec;
    float RMSE;
    
    User(){}
    
    User(int theSequence, float theAvgStar, int theReviewCount, const std::string &theName)
    :sequence(theSequence), avgStar(theAvgStar), reviewCount(theReviewCount), name(theName),
    RMSE(0)
    {
        starVec.push_back(theAvgStar);
    }
};


#endif /* defined(__RecSys2013__User__) */
