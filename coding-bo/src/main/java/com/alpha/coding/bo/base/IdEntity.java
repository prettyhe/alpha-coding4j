package com.alpha.coding.bo.base;

import java.io.Serializable;

import javax.persistence.Id;
import javax.validation.constraints.NotNull;

import lombok.Data;

/**
 * IdEntity
 *
 * @version 1.0
 * Date: 2016-9-5
 */
@Data
public abstract class IdEntity implements Serializable {

    @Id
    @NotNull(message = "id非法")
    protected Long id;
}
