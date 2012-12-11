package com.example;

public class BMICalc {
  protected float weight = 0; // センチメートルで

  protected float height = 0; // キログラムで

  public float getBmi() {
    return height == 0 ? 0 : weight / height / height * 10000;
    // height が未設定のときは 0 を返す
  }

  public float getHeight() {
    return height;
  }

  public void setHeight(float height) {
    this.height = height;
  }

  public float getWeight() {
    return weight;
  }

  public void setWeight(float weight) {
    this.weight = weight;
  }

}