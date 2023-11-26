
-- noinspection SqlNoDataSourceInspectionForFile
CREATE TABLE IF NOT EXISTS Notes (
        guid                    UUID            NOT NULL PRIMARY KEY,
        externalDataSource      varchar(50)     NOT NULL,
        externalGuid            UUID            NOT NULL,
        externalItemGuid        UUID            NOT NULL,
        externalItemId          varchar(50)     ,
        threadGuid              UUID            ,
        entryGuid               UUID            NOT NULL,
        entryGuidParent         UUID            ,
        userId                  varchar(20)     ,
        type                    varchar(20)     NOT NULL,
        content                 varchar(2000)   NOT NULL,
        customJson              jsonb           ,
        createdInitially        timestamptz     NOT NULL,
        created                 timestamptz     NOT NULL,
        isDirty                 bool            NOT NULL DEFAULT TRUE

);

CREATE INDEX IF NOT EXISTS Notes_externalGuid_idx ON Notes (externalGuid);
CREATE INDEX IF NOT EXISTS Notes_threadGuid_idx ON Notes (threadGuid);
CREATE INDEX IF NOT EXISTS Notes_entryGuid_idx ON Notes (entryGuid);
