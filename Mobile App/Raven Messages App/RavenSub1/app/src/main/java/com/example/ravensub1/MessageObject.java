package com.example.ravensub1;

import java.util.Date;

public class MessageObject {
        private int id;
        private String topic;
        private String message;
        private Date date;

        public MessageObject() {
            this.id = 0;
            this.topic = "";
            this.message = "";
            this.date = null;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getTopic() {
            return topic;
        }

        public void setTopic(String topic) {
            this.topic = topic;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public Date getDate() {
            return date;
        }

        public void setDate(Date date) {
            this.date = date;
        }


    public void clear() {
        this.id = 0;
        this.topic = "";
        this.message = "";
        this.date = null;
    }
}
