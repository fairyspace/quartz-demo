package io.github.fairyspace.quartzdemo.common;

import lombok.Data;

import java.io.Serializable;

@Data
public class JobInfo implements Serializable {
    private static final long serialVersionUID = 8026140551673459050L;
    private int id;
    private String jobname;
    private String jobgroup;
    private String jobclassname;
    private String triggername;
    private String triggergroup;
    private String cronexpression;
    private String description;
    private String startfiretime;
    private String nextfiretime;
    private String endfiretime;
    private String state;

    private Integer repeatcount;
    private Integer intervalsecond;


}
