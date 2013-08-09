ResultTable <- read.csv("wResult.csv", head=T);

summary(ResultTable)

ori <- ResultTable$AvgFromReviews
w1 <- ResultTable$wIDF
w2 <- ResultTable$wAvg
w3 <- ResultTable$wRM
w4 <- ResultTable$wBase

n <- length(ori)

RMSE1 <- sqrt(sum((ori-w1)^2)/n)
RMSE2 <- sqrt(sum((ori-w2)^2)/n)
RMSE3 <- sqrt(sum((ori-w3)^2)/n)
RMSE4 <- sqrt(sum((ori-w4)^2)/n)