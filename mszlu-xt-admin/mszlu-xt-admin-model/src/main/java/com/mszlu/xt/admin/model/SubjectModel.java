package com.mszlu.xt.admin.model;

import lombok.Data;

/**
 * @author Jarno
 */
@Data
public class SubjectModel {
    private Long id;
    private String subjectName;
    private String subjectGrade;
    private String subjectTerm;
    private Integer status;

    public void fillSubjectName() {
        this.subjectName = this.subjectName + "-" + this.subjectGrade + "-" + this.subjectTerm;
    }
}
