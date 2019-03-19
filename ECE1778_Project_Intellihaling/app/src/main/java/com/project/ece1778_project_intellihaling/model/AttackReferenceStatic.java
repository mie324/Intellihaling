package com.project.ece1778_project_intellihaling.model;

import java.util.HashMap;
import java.util.Map;

public class AttackReferenceStatic {

    public static int[][] attackRef =
            new int[][]{
                    {43, 147}, {44, 160}, {45, 173}, {46, 187}, {47, 200}, {48, 214},
                    {49, 227}, {50, 240}, {51, 254}, {52, 267}, {53, 280}, {54, 293},
                    {55, 307}, {56, 320}, {57, 334}, {58, 347}, {59, 360}, {60, 373},
                    {61, 387}, {62, 400}, {63, 413}, {64, 427}, {65, 440}, {66, 454}};

    public static Map<Integer, Integer> attRefMap = createMap();

    private static Map<Integer, Integer> createMap() {

        Map<Integer, Integer> mapTmp = new HashMap<Integer, Integer>();
        mapTmp.put(43, 147);
        mapTmp.put(44, 160);
        mapTmp.put(45, 173);
        mapTmp.put(46, 187);
        mapTmp.put(47, 200);
        mapTmp.put(48, 214);
        mapTmp.put(49, 227);
        mapTmp.put(50, 240);
        mapTmp.put(51, 254);
        mapTmp.put(52, 267);
        mapTmp.put(53, 280);
        mapTmp.put(54, 293);
        mapTmp.put(55, 307);
        mapTmp.put(56, 320);
        mapTmp.put(57, 334);
        mapTmp.put(58, 347);
        mapTmp.put(59, 360);
        mapTmp.put(60, 373);
        mapTmp.put(61, 387);
        mapTmp.put(62, 400);
        mapTmp.put(63, 413);
        mapTmp.put(64, 427);
        mapTmp.put(65, 440);
        mapTmp.put(66, 454);

        return mapTmp;
    }
}
