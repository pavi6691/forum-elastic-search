
-- noinspection SqlNoDataSourceInspectionForFile
CREATE TABLE IF NOT EXISTS Notes (
        guid                    UUID            NOT NULL PRIMARY KEY,
        externalGuid            UUID            NOT NULL,
        threadGuid              UUID            ,
        entryGuid               UUID            NOT NULL,
        entryGuidParent         UUID            ,
        type                    varchar(20)     NOT NULL,
        content                 varchar(2000)   NOT NULL,
        customJson              jsonb           ,
        created                 timestamptz     NOT NULL

);

CREATE INDEX IF NOT EXISTS Notes_externalGuid_idx ON Notes (externalGuid);
CREATE INDEX IF NOT EXISTS Notes_threadGuid_idx ON Notes (threadGuid);
CREATE INDEX IF NOT EXISTS Notes_entryGuid_idx ON Notes (entryGuid);
