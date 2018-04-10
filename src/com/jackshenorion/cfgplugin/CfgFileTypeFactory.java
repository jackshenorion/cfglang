package com.jackshenorion.cfgplugin;

import com.intellij.openapi.fileTypes.*;
import org.jetbrains.annotations.NotNull;

public class CfgFileTypeFactory extends FileTypeFactory {
    @Override
    public void createFileTypes(@NotNull FileTypeConsumer fileTypeConsumer) {
        fileTypeConsumer.consume(CfgFileType.INSTANCE,
                new WildcardFileNameMatcher(CfgFileType.PREFIX_SMARTSCONTROL + "*.cfg"),
                new WildcardFileNameMatcher(CfgFileType.PREFIX_STANDARDCONTROL + "*.cfg")
        );
    }
}
