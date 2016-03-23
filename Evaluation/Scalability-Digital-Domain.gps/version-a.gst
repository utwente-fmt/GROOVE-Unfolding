<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<gxl xmlns="http://www.gupro.de/GXL/gxl-1.0.dtd">
    <graph role="graph" edgeids="false" edgemode="directed" id="version-a">
        <attr name="$version">
            <string>curly</string>
        </attr>
        <node id="n0">
            <attr name="layout">
                <string>776 660 69 24</string>
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
        <edge from="n0" to="n0">
            <attr name="label">
                <string>type:Attacker</string>
            </attr>
        </edge>
        <edge from="n0" to="n4">
            <attr name="label">
                <string>at</string>
            </attr>
        </edge>
        <edge from="n4" to="n4">
            <attr name="label">
                <string>id:Internet</string>
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
                <string>type:LocationDigital</string>
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
        <edge from="n6" to="n6">
            <attr name="label">
                <string>type:LocationDigital</string>
            </attr>
        </edge>
        <edge from="n6" to="n1">
            <attr name="label">
                <string>policy</string>
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
        <edge from="n9" to="n10">
            <attr name="label">
                <string>connectedTo</string>
            </attr>
            <attr name="layout">
                <string>268 -23 164 524 540 411 11</string>
            </attr>
        </edge>
        <edge from="n9" to="n3">
            <attr name="label">
                <string>contains</string>
            </attr>
        </edge>
        <edge from="n10" to="n10">
            <attr name="label">
                <string>id:Gateway</string>
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
        <edge from="n10" to="n6">
            <attr name="label">
                <string>connectedTo</string>
            </attr>
            <attr name="layout">
                <string>289 -23 540 411 546 248 11</string>
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
    </graph>
</gxl>
