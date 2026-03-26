### AI算法训练平台目录结构

```markdown
├─ AI 算法训练平台
└─ mnt/sda/                                           # 根目录
    ├─ dataset/                                       # 数据集根目录
    |   └─ xxx数据集（基础）/                            # xxx数据集目录
    |       ├─ base/                                  # 基础数据集
    |       |   ├─ label/                             # 基础数据集标签目录                            
    |       |   |   └─ xxx initial/                   # 基础数据集初始标签目录  
    |       |   |       ├─ XXX classify/              # 基础数据集分类标签存放目录
    |       |   |       └─ 基础数据集初始classify标签.json
    |       |   └─ imgs                               # 基础数据集图像目录
    |       |       └─ xxx.png
    |       └─ version xxx/                           # 版本xxx数据集目录
    |           └─ label/                             # 版本xxx数据集标签目录
    |               └─ initial/                       # 版本xxx数据集初始标签目录（若有，则拷贝于基础数据集初始标签目录）
    |                 ├─ XXX classify/                # 数据集分类标签存放目录
    |                 └─ 数据集初始classify标签.json
    └─ model/                                         # 模型根目录
    |    └─ xxx基础模型/                                 # xxx模型根目录(只存放配置文件)
    |       ├─ base/
    |       |   └─ config/                            # 基础模型训练配置目录
    |       |       ├─ trainConfig.json               # 训练配置文件
    |       |       └─ config.json                    # 模型配置文件
    |       └─ xxx模型/                                # xxx模型(修改模型配置文件产生的模型)
    |           └─ config/                            # 模型训练配置目录(模型配置\训练配置:通过算法服务器获取)
    |               ├─ trainConfig.json               # 训练配置文件
    |               └─ config.json （创建之后不能修改）   # 模型配置文件
    └─ program/
        └─ xxx 项目/          
            ├─ xxx训练实验ID/                  			  # 某次训练目录
            |   ├─ config/                            # 某次训练配置目录
            |   |   └─ config.json                    # 某次训练模型配置文件(从基础模型配置文件中读取)
            |   └─ checkpoint/                  	  # 训练权重目录(由算法服务器存储过来)
            |       └─ xxx.pt
            ├─ xxx校验实验ID/           			 		  # 某次训练目录
            |   ├─ config/                  		  # 某次训练配置目录
            |   |   └─ config.json 					  # 某次训练模型配置文件
            |   └─ predict/  存标签 					
            |   |   └─ xxx.json
            |   └─ result 存校验结果/
            |       └─ xxx.json
            └─ xxx推理（预测）实验ID/           			  # 某次训练目录
                ├─ config/                  		  # 某次训练配置目录
                |   └─ config.json 					  # 某次训练模型配置文件
                └─ predict/  存标签 					
                    └─ xxx.json


```

