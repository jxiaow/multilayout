# MultiLayout

[![](https://jitpack.io/v/ixiaow/multilayout.svg)](https://jitpack.io/#ixiaow/multilayout)

一个可以支持多行的TabLayout的Android库。

目前主要支持的功能有：

* 可以添加多行Tab
* 可以和ViewPager联合使用
* 具备TabLayout的相关功能

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
    implementation 'com.github.ixiaow:multilayout:12f3374a0e'
}
```

### Step2

在你自己的`xml`文件中使用：

```xml
<com.ixiaow.multilayout.MultiLayout
     android:id="@+id/multi_layout"
     android:layout_width="match_parent"
     android:layout_height="wrap_content" />
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
mMultiLayout.setTabNames(tabNames);
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

## 截图

![img](https://github.com/ixiaow/multilayout/blob/master/picture/demo.gif)

## Demo

[sample](https://github.com/ixiaow/multilayout/tree/master/sample)

