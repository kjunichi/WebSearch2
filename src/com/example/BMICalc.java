package com.example;

public class BMICalc {
  protected float weight = 0; // �Z���`���[�g����

  protected float height = 0; // �L���O������

  public float getBmi() {
    return height == 0 ? 0 : weight / height / height * 10000;
    // height �����ݒ�̂Ƃ��� 0 ��Ԃ�
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