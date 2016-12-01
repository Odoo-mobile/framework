/**
 * Odoo, Open Source Management Solution
 * Copyright (C) 2012-today Odoo SA (<http:www.odoo.com>)
 * <p/>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version
 * <p/>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details
 * <p/>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http:www.gnu.org/licenses/>
 * <p/>
 * Created on 31/12/14 6:32 PM
 */
package com.odoo.core.orm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
public @interface Odoo {

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface SyncColumnName {
        String value() default "";
    }

    /**
     * The Interface Functional.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface Functional {

        /**
         * Method.
         *
         * @return the string
         */
        String method() default "";

        /**
         * If true, system create column for this functional field and store
         * value (on create and update) given by this function
         *
         * @return true, if successful
         */
        boolean store() default false;

        /**
         * Depends.
         *
         * @return the string[]
         */
        String[] depends() default {};

    }

    @Retention(RetentionPolicy.RUNTIME)
    @interface api {

        @Retention(RetentionPolicy.RUNTIME)
        @Target({ElementType.FIELD, ElementType.METHOD})
        @interface v7 {
            String[] versions() default {};

            String[] exclude() default {};
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target({ElementType.FIELD, ElementType.METHOD})
        @interface v8 {
            String[] versions() default {};

            String[] exclude() default {};
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target({ElementType.FIELD, ElementType.METHOD})
        @interface v9 {
            String[] versions() default {};

            String[] exclude() default {};
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target({ElementType.FIELD, ElementType.METHOD})
        @interface v10 {
            String[] versions() default {};

            String[] exclude() default {};
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Target({ElementType.FIELD, ElementType.METHOD})
        @interface v11alpha {
            String[] versions() default {};

            String[] exclude() default {};
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.METHOD})
    @interface onChange {
        String method();

        /**
         * Background process If true, method block executed in background
         * thread. default false
         *
         * @return boolean flag
         */
        boolean bg_process() default false;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.METHOD})
    @interface Domain {
        String value() default "";
    }

}
