package com.giraone.io.copier.web.index;

/*
[
{ "name":"common", "type":"directory", "mtime":"Fri, 21 Oct 2022 13:29:36 GMT" },
{ "name":"lohn-journal", "type":"directory", "mtime":"Tue, 16 Aug 2022 07:28:42 GMT" },
{ "name":"index.txt", "type":"file", "mtime":"Wed, 12 Oct 2022 12:58:04 GMT", "size":2693 }
]
 */

import com.fasterxml.jackson.annotation.JsonIgnore;

public class AutoIndexItem {
    private String name;
    private AutoIndexItemType type;

    // Needed for Jackson
    @SuppressWarnings("unused")
    public AutoIndexItem() {
    }

    public AutoIndexItem(String name, AutoIndexItemType type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AutoIndexItemType getType() {
        return type;
    }

    @SuppressWarnings("unused")
    public void setType(AutoIndexItemType type) {
        this.type = type;
    }

    @JsonIgnore
    public boolean isDirectory() {
        return type == AutoIndexItemType.directory;
    }

    @Override
    public String toString() {
        return "AutoIndexItem{" +
            "name='" + name + '\'' +
            ", type='" + type + '\'' +
            '}';
    }

}
