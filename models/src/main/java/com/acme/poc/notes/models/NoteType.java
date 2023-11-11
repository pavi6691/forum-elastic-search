package com.acme.poc.notes.models;

import java.util.List;


/**
 * A note can be of a normal (default) type of note or a specific type.
 * This class contains the different types of notes that can be created and also specifies:
 * <ul>
 *     <li>if it is allowed as a root note in the thread</li>
 *     <li>if custom JSON can be stored with the note</li>
 *     <li>which types of notes can be a direct child of the specific note type</li>
 * </ul>
 */
public enum NoteType {

    NOTE   ("note"  , true , true ) { @Override boolean isNote()   { return true; }},
    REMARK ("remark", false, false) { @Override boolean isRemark() { return true; }};


    static {
        NOTE.allowedChildTypes   = List.of(NOTE, REMARK);
        REMARK.allowedChildTypes = null;
    }


    private final String typeValue;
    private final boolean allowAsRoot;
    private List<NoteType> allowedChildTypes;
    private final boolean allowCustomJson;


    NoteType(String typeValue, boolean allowAsRoot, boolean allowCustomJson) {
        this.typeValue = typeValue;
        this.allowAsRoot = allowAsRoot;
        this.allowCustomJson = allowCustomJson;
    }


    boolean isNote()   { return false; }
    boolean isRemark() { return false; }

    String typeValue() { return typeValue; }
    boolean allowAsRoot() { return allowAsRoot; }
    List<NoteType> allowedChildTypes() { return allowedChildTypes; }
    boolean allowCustomJson() { return allowCustomJson; }

}
