package com.kanokna.shared.measure;

public enum ThicknessTypeMM {
    MM_58(58),
    MM_60(60),
    MM_70(70),
    MM_76(76),
    MM_80(80),
    MM_86(86);

    private final Integer value;
    
    ThicknessType(Integer value) {
        this.value = value;
    }
    
    public Integer getValue() { 
        return value; 
    }
}
