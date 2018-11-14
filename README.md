# MultiLayout

[![](https://jitpack.io/v/ixiaow/multilayout.svg)](https://jitpack.io/#ixiaow/multilayout)

一个可以支持多行的TabLayout的Android库。

目前主要支持的功能有：

* 根据数据可以动态拆分添加多行Tab
* 可以和ViewPager联合使用
* 具备TabLayout的相关功能

## 截图

![img](https://github.com/ixiaow/multilayout/blob/master/picture/demo.gif)

## 用法

### Step 1

在根`build.gradle`中添加：

```groovy
allprojects {
    repositories {
        maven { url "https://jitpack.io" }
    }
}
```

在使用的模块`build.gradle`中添加：

```groovy
dependencies {
    implementation 'com.github.ixiaow:multilayout:x.y.z'
}
```
(请替换 x. y .z 为最新的版本号: ![](https://jitpack.io/v/ixiaow/multilayout.svg) )

### Step2

在你自己的`xml`文件中使用：

```xml
<com.ixiaow.multilayout.MultiLayout
     android:id="@+id/topic_layout"
     android:layout_width="match_parent"
     android:layout_height="wrap_content"
     app:tab_indicator_color="@color/colorAccent"
     app:tab_indicator_height="5dp"
     app:tab_indicator_radius="5dp"
     app:tab_text_select_color="@color/colorAccent"
     app:tab_text_size="14sp"
     app:tab_text_unselect_color="@color/colorPrimary" />
```

### Step3

在`Activity`或`Fragment`中初始化：

```java
private MultiLayout mMultiLayout;
mMultiLayout = findViewById(R.id.multi_layout);
```

普通使用：

```java
//tabNames是一个String类型的List集合，代表tab的名称
mMultiLayout.initTabNames(tabNames);
//更新tabNames
mMultiLayout.updateTabNames(tabNames);
```
与`ViewPager`组合使用：

```java
mViewPager.setAdapter(mCatalogAdapter);
mMultiLayout.setupWithViewPager(mViewPager);
```
添加`TabText`的点击事件：

```java
/**
  * 设置Tab选择监听事件
  *
  * @param onTabSelectListener tab监听事件
  */
  public void setOnTabSelectListener(OnTabSelectListener onTabSelectListener) {
      this.mOnTabSelectListener = onTabSelectListener;
  }
```

## Demo

具体的使用请看：[sample](https://github.com/ixiaow/multilayout/tree/master/sample)



## 扩展属性

`multilayout`支持一下扩展属性:

|        Attribute        |  format   |        description        |
| :---------------------: | :-------: | :-----------------------: |
|     tab_text_width      | dimension |       tab文本的宽度       |
|     tab_text_height     | dimension |       tab文本的高度       |
|      tab_text_size      | dimension |     tab文本的字体大小     |
|  tab_text_select_color  |   color   |  tab文本选中时的字体颜色  |
| tab_text_unselect_color |   color   | tab文本字体未选中时的颜色 |
|   tab_indicator_color   |   color   |   indicator指示器的颜色   |
|   tab_indicator_width   | dimension |   indicator指示器的宽度   |
|  tab_indicator_height   | dimension |   indicator指示器的高度   |
|  tab_indicator_radius   | dimension | indicator指示器的圆角弧度 |





