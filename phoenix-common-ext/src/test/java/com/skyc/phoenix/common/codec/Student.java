package com.skyc.phoenix.common.codec;

import java.util.List;
import java.util.Map;

public class Student {
    private String name;
    private int age;
    private Street street;
    private List<String> others;
    private Map<String, String> othersMap;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public Street getStreet() {
        return street;
    }

    public void setStreet(Street street) {
        this.street = street;
    }

    public List<String> getOthers() {
        return others;
    }

    public void setOthers(List<String> others) {
        this.others = others;
    }

    public Map<String, String> getOthersMap() {
        return othersMap;
    }

    public void setOthersMap(Map<String, String> othersMap) {
        this.othersMap = othersMap;
    }

    @Override
    public String toString() {
        return "Student{" + "name='" + name + '\'' + ", age=" + age + ", street=" + street + ", others=" + others
                       + ", othersMap=" + othersMap + '}';
    }

    public static class Street {
        private String road;
        private String door;
        private int num;

        public String getRoad() {
            return road;
        }

        public void setRoad(String road) {
            this.road = road;
        }

        public String getDoor() {
            return door;
        }

        public void setDoor(String door) {
            this.door = door;
        }

        public int getNum() {
            return num;
        }

        public void setNum(int num) {
            this.num = num;
        }

        @Override
        public String toString() {
            return "Street{" + "road='" + road + '\'' + ", door='" + door + '\'' + ", num=" + num + '}';
        }
    }
}
