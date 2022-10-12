package com.amerd.schoolbook.domain.user;

import java.util.Map;

public enum UserSetter implements FieldSetter<User> {
    username {
        @Override
        public void setFieldValue(User user, Map<String, String> updateValues) {
            String value = getValue(updateValues, User.Fields.username);
            if (value != null && !value.isBlank()) {
                user.setUsername(value);
            }
        }
    },
    password {
        @Override
        public void setFieldValue(User user, Map<String, String> updateValues) {
            String value = getValue(updateValues, User.Fields.password);
            if (value != null && !value.isBlank()) {
                user.setPassword(value);
            }
        }
    },
    firstName {
        @Override
        public void setFieldValue(User user, Map<String, String> updateValues) {
            String value = getValue(updateValues, User.Fields.firstName);
            if (value != null && !value.isBlank()) {
                user.setFirstName(value);
            }
        }
    },
    lastName {
        @Override
        public void setFieldValue(User user, Map<String, String> updateValues) {
            String value = getValue(updateValues, User.Fields.lastName);
            if (value != null && !value.isBlank()) {
                user.setLastName(value);
            }
        }
    },
    email {
        @Override
        public void setFieldValue(User user, Map<String, String> updateValues) {
            String value = getValue(updateValues, User.Fields.email);
            if (value != null && !value.isBlank()) {
                user.setEmail(value);
            }
        }
    }
}
