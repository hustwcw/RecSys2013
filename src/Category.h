//
//  Category.h
//  RecSys2013
//
//  Created by JTangWang on 13-8-6.
//  Copyright (c) 2013年 JTangWang. All rights reserved.
//

#ifndef __RecSys2013__Category__
#define __RecSys2013__Category__

#include <iostream>


class Category {
public:
    float avgStar;
    int businessCnt;
    int reviewCnt;
    float RMSE;
    
    Category(float avg, int bCnt, int rCnt, float rmse)
    :avgStar(avg), businessCnt(bCnt), reviewCnt(rCnt), RMSE(rmse)
    {}
};


#endif /* defined(__RecSys2013__Category__) */
