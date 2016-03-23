<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<gxl xmlns="http://www.gupro.de/GXL/gxl-1.0.dtd">
    <graph role="graph" edgeids="false" edgemode="directed" id="version-c">
        <attr name="$version">
            <string>curly</string>
        </attr>
        <node id="n0">
            <attr name="layout">
                <string>1156 692 69 24</string>
            </attr>
        </node>
        <node id="n4">
            <attr name="layout">
                <string>307 677 196 48</string>
            </attr>
        </node>
        <node id="n6">
            <attr name="layout">
                <string>465 236 162 24</string>
            </attr>
        </node>
        <node id="n2">
            <attr name="layout">
                <string>125 236 128 24</string>
            </attr>
        </node>
        <node id="n1">
            <attr name="layout">
                <string>406 91 221 24</string>
            </attr>
        </node>
        <node id="n9">
            <attr name="layout">
                <string>71 512 187 24</string>
            </attr>
        </node>
        <node id="n10">
            <attr name="layout">
                <string>440 399 201 24</string>
            </attr>
        </node>
        <node id="n3">
            <attr name="layout">
                <string>17 374 226 24</string>
            </attr>
        </node>
        <node id="n5">
            <attr name="layout">
                <string>1099 533 201 24</string>
            </attr>
        </node>
        <node id="n7">
            <attr name="layout">
                <string>1103 390 188 24</string>
            </attr>
        </node>
        <node id="n8">
            <attr name="layout">
                <string>1077 228 232 24</string>
            </attr>
        </node>
        <node id="n11">
            <attr name="layout">
                <string>795 233 137 24</string>
            </attr>
        </node>
        <node id="n12">
            <attr name="layout">
                <string>769 311 186 24</string>
            </attr>
        </node>
        <node id="n13">
            <attr name="layout">
                <string>1071 88 221 24</string>
            </attr>
        </node>
        <node id="n14">
            <attr name="layout">
                <string>783 413 141 24</string>
            </attr>
        </node>
        <edge from="n0" to="n4">
            <attr name="label">
                <string>at</string>
            </attr>
        </edge>
        <edge from="n0" to="n5">
            <attr name="label">
                <string>at</string>
            </attr>
        </edge>
        <edge from="n0" to="n0">
            <attr name="label">
                <string>type:Attacker</string>
            </attr>
        </edge>
        <edge from="n4" to="n4">
            <attr name="label">
                <string>id:Internet</string>
            </attr>
        </edge>
        <edge from="n4" to="n4">
            <attr name="label">
                <string>type:LocationDigital</string>
            </attr>
        </edge>
        <edge from="n4" to="n9">
            <attr name="label">
                <string>connectedTo</string>
            </attr>
            <attr name="layout">
                <string>580 -34 405 701 164 524 11</string>
            </attr>
        </edge>
        <edge from="n4" to="n4">
            <attr name="label">
                <string>flag:Outside</string>
            </attr>
        </edge>
        <edge from="n4" to="n10">
            <attr name="label">
                <string>connectedTo</string>
            </attr>
            <attr name="layout">
                <string>582 -16 405 701 540 411 11</string>
            </attr>
        </edge>
        <edge from="n6" to="n6">
            <attr name="label">
                <string>id:VM</string>
            </attr>
        </edge>
        <edge from="n6" to="n2">
            <attr name="label">
                <string>contains</string>
            </attr>
        </edge>
        <edge from="n6" to="n1">
            <attr name="label">
                <string>policy</string>
            </attr>
        </edge>
        <edge from="n6" to="n6">
            <attr name="label">
                <string>type:LocationDigital</string>
            </attr>
        </edge>
        <edge from="n2" to="n2">
            <attr name="label">
                <string>id:FileX</string>
            </attr>
        </edge>
        <edge from="n2" to="n2">
            <attr name="label">
                <string>type:AssetData</string>
            </attr>
        </edge>
        <edge from="n1" to="n1">
            <attr name="label">
                <string>id:Password</string>
            </attr>
        </edge>
        <edge from="n1" to="n1">
            <attr name="label">
                <string>type:CredentialDigital</string>
            </attr>
        </edge>
        <edge from="n9" to="n9">
            <attr name="label">
                <string>id:Laptop</string>
            </attr>
        </edge>
        <edge from="n9" to="n9">
            <attr name="label">
                <string>type:LocationDigital</string>
            </attr>
        </edge>
        <edge from="n9" to="n3">
            <attr name="label">
                <string>contains</string>
            </attr>
        </edge>
        <edge from="n9" to="n10">
            <attr name="label">
                <string>connectedTo</string>
            </attr>
            <attr name="layout">
                <string>268 -23 164 524 540 411 11</string>
            </attr>
        </edge>
        <edge from="n10" to="n10">
            <attr name="label">
                <string>id:Gateway</string>
            </attr>
        </edge>
        <edge from="n10" to="n6">
            <attr name="label">
                <string>connectedTo</string>
            </attr>
            <attr name="layout">
                <string>289 -23 540 411 546 248 11</string>
            </attr>
        </edge>
        <edge from="n10" to="n10">
            <attr name="label">
                <string>type:LocationDigital</string>
            </attr>
        </edge>
        <edge from="n10" to="n3">
            <attr name="label">
                <string>policy</string>
            </attr>
        </edge>
        <edge from="n3" to="n3">
            <attr name="label">
                <string>id:Certificate</string>
            </attr>
        </edge>
        <edge from="n3" to="n3">
            <attr name="label">
                <string>type:CredentialDigital</string>
            </attr>
        </edge>
        <edge from="n5" to="n5">
            <attr name="label">
                <string>id:Outside</string>
            </attr>
        </edge>
        <edge from="n5" to="n7">
            <attr name="label">
                <string>connectedTo</string>
            </attr>
        </edge>
        <edge from="n5" to="n5">
            <attr name="label">
                <string>type:LocationPhysical</string>
            </attr>
        </edge>
        <edge from="n7" to="n7">
            <attr name="label">
                <string>id:Inside</string>
            </attr>
        </edge>
        <edge from="n7" to="n14">
            <attr name="label">
                <string>contains</string>
            </attr>
        </edge>
        <edge from="n7" to="n12">
            <attr name="label">
                <string>policy</string>
            </attr>
        </edge>
        <edge from="n7" to="n7">
            <attr name="label">
                <string>type:LocationPhysical</string>
            </attr>
        </edge>
        <edge from="n7" to="n8">
            <attr name="label">
                <string>connectedTo</string>
            </attr>
        </edge>
        <edge from="n8" to="n8">
            <attr name="label">
                <string>id:ServerRoom</string>
            </attr>
        </edge>
        <edge from="n8" to="n11">
            <attr name="label">
                <string>contains</string>
            </attr>
        </edge>
        <edge from="n8" to="n8">
            <attr name="label">
                <string>type:LocationPhysical</string>
            </attr>
        </edge>
        <edge from="n8" to="n13">
            <attr name="label">
                <string>policy</string>
            </attr>
        </edge>
        <edge from="n11" to="n11">
            <attr name="label">
                <string>id:Server</string>
            </attr>
        </edge>
        <edge from="n11" to="n6">
            <attr name="label">
                <string>contains</string>
            </attr>
        </edge>
        <edge from="n11" to="n11">
            <attr name="label">
                <string>type:AssetItem</string>
            </attr>
        </edge>
        <edge from="n12" to="n12">
            <attr name="label">
                <string>id:Key</string>
            </attr>
        </edge>
        <edge from="n12" to="n12">
            <attr name="label">
                <string>type:CredentialPhysical</string>
            </attr>
        </edge>
        <edge from="n13" to="n13">
            <attr name="label">
                <string>id:KeyCard</string>
            </attr>
        </edge>
        <edge from="n13" to="n13">
            <attr name="label">
                <string>type:CredentialPhysical</string>
            </attr>
        </edge>
        <edge from="n14" to="n14">
            <attr name="label">
                <string>id:Router</string>
            </attr>
        </edge>
        <edge from="n14" to="n10">
            <attr name="label">
                <string>contains</string>
            </attr>
        </edge>
        <edge from="n14" to="n14">
            <attr name="label">
                <string>type:AssetItem</string>
            </attr>
        </edge>
    </graph>
</gxl>
