import os
import pandas as pd
import numpy as np
from sklearn.model_selection import train_test_split
from sklearn.pipeline import Pipeline
from sklearn.preprocessing import StandardScaler
from sklearn.linear_model import LinearRegression
from sklearn import metrics

if __name__ == '__main__':
    dataset_path = os.path.join('../lab', 'USA_Housing.csv')

    # 读取数据
    df = pd.read_csv(dataset_path)

    # Address 是唯一值，没有训练意义，舍弃该特征
    df.drop('Address', axis=1, inplace=True)

    # 前十条示例数据
    print("示例数据：")
    print(df.head(10))

    y = df.get('Price')
    X = df.drop('Price', axis=1)
    X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.3)
    test_df=X_test

    pipeline = Pipeline([
        ('std_scalar', StandardScaler())
    ])

    X_train = pipeline.fit_transform(X_train)
    X_test = pipeline.transform(X_test)

    # 线性回归
    lin_reg = LinearRegression(normalize=True)
    lin_reg.fit(X_train, y_train)
    y_pred = lin_reg.predict(X_test)

    test_df['Price'] = y_test
    test_df['Prediction'] = y_pred

    # 查看预测结果
    print(f"预测结果:{test_df[['Price', 'Prediction']]}")
    path = 'res.csv'
    test_df.to_csv(path, index=False)

    # 评估
    def print_evaluate(true, predicted):
        mae = metrics.mean_absolute_error(true, predicted)
        mse = metrics.mean_squared_error(true, predicted)
        rmse = np.sqrt(metrics.mean_squared_error(true, predicted))
        r2_square = metrics.r2_score(true, predicted)
        print('MAE:', mae)# 平均绝对误差
        print('MSE:', mse)# 平均平方误差
        print('RMSE:', rmse)# 均方根误差
        print('R2 Square', r2_square)# 决定系数
        print('__________________________________')

    test_pred = lin_reg.predict(X_test)
    train_pred = lin_reg.predict(X_train)

    print('Test set evaluation:')
    print_evaluate(y_test, test_pred)
    print('Train set evaluation:')
    print_evaluate(y_train, train_pred)
