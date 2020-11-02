package com.zhanghui.support.constants;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

/**
 * @author: ZhangHui
 * @date: 2020/10/14 9:30
 * @version：1.0
 */
@Getter
@Setter
@Builder
public class RpcProvider {

    private String ref;

    private String interfaceName;

    private String group;

    private String version;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RpcProvider that = (RpcProvider) o;
        return Objects.equals(interfaceName, that.interfaceName)
                && Objects.equals(group, that.group)
                && Objects.equals(version, that.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(interfaceName, group, version);
    }
}
