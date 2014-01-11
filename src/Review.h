//
//  Review.h
//  RecSys2013
//
//  Created by JTangWang on 13-7-24.
//  Copyright (c) 2013年 JTangWang. All rights reserved.
//

#ifndef __RecSys2013__Review__
#define __RecSys2013__Review__

#include <iostream>
#include <string>

struct Review {
    std::string uid;
    std::string bid;
    float star;
    int date; // 评分时间，以从公元元年到当前日期经过的天数计算，按每年365天，每月30天计算。
    Review(std::string u, std::string b, float s, int d)
    :uid(u), bid(b), star(s), date(d)
    {}
    
    bool operator < (const Review &other) const
    {
        if (uid < other.uid || (uid == other.uid && bid < other.bid)) {
            return true;
        }
        else
        {
            return false;
        }
    }
    
    bool operator == (const Review &other) const
    {
        return (uid == other.uid && bid == other.bid);
    }
};
    
    
#endif /* defined(__RecSys2013__Review__) */
