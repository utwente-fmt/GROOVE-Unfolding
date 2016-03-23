<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<gxl xmlns="http://www.gupro.de/GXL/gxl-1.0.dtd">
    <graph role="graph" edgeids="false" edgemode="directed" id="version-c">
        <attr name="$version">
            <string>curly</string>
        </attr>
        <node id="n0">
            <attr name="layout">
                <string>987 752 69 24</string>
            </attr>
        </node>
        <node id="n4">
            <attr name="layout">
                <string>445 736 196 48</string>
            </attr>
        </node>
        <node id="n6">
            <attr name="layout">
                <string>465 96 162 24</string>
            </attr>
        </node>
        <node id="n2">
            <attr name="layout">
                <string>216 91 128 24</string>
            </attr>
        </node>
        <node id="n1">
            <attr name="layout">
                <string>431 8 221 24</string>
            </attr>
        </node>
        <node id="n9">
            <attr name="layout">
                <string>88 574 187 24</string>
            </attr>
        </node>
        <node id="n10">
            <attr name="layout">
                <string>455 590 201 24</string>
            </attr>
        </node>
        <node id="n3">
            <attr name="layout">
                <string>35 677 226 24</string>
            </attr>
        </node>
        <node id="n5">
            <attr name="layout">
                <string>473 296 123 24</string>
            </attr>
        </node>
        <node id="n7">
            <attr name="layout">
                <string>480 413 123 24</string>
            </attr>
        </node>
        <node id="n8">
            <attr name="layout">
                <string>773 595 123 24</string>
            </attr>
        </node>
        <node id="n11">
            <attr name="layout">
                <string>759 440 123 24</string>
            </attr>
        </node>
        <node id="n12">
            <attr name="layout">
                <string>922 354 138 24</string>
            </attr>
        </node>
        <node id="n13">
            <attr name="layout">
                <string>1066 460 123 24</string>
            </attr>
        </node>
        <node id="n14">
            <attr name="layout">
                <string>1068 239 123 24</string>
            </attr>
        </node>
        <node id="n15">
            <attr name="layout">
                <string>1075 96 138 24</string>
            </attr>
        </node>
        <node id="n16">
            <attr name="layout">
                <string>1049 598 123 24</string>
            </attr>
        </node>
        <node id="n17">
            <attr name="layout">
                <string>153 404 123 24</string>
            </attr>
        </node>
        <node id="n18">
            <attr name="layout">
                <string>34 280 138 24</string>
            </attr>
        </node>
        <node id="n19">
            <attr name="layout">
                <string>846 97 123 24</string>
            </attr>
        </node>
        <node id="n20">
            <attr name="layout">
                <string>482 197 123 24</string>
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
                <string>type:LocationDigital</string>
            </attr>
        </edge>
        <edge from="n4" to="n16">
            <attr name="label">
                <string>connectedTo</string>
            </attr>
        </edge>
        <edge from="n4" to="n9">
            <attr name="label">
                <string>connectedTo</string>
            </attr>
            <attr name="layout">
                <string>371 -25 543 760 181 586 11</string>
            </attr>
        </edge>
        <edge from="n4" to="n8">
            <attr name="label">
                <string>connectedTo</string>
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
                <string>582 -16 543 760 555 602 11</string>
            </attr>
        </edge>
        <edge from="n6" to="n6">
            <attr name="label">
                <string>id:VM</string>
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
        <edge from="n6" to="n2">
            <attr name="label">
                <string>contains</string>
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
        <edge from="n9" to="n17">
            <attr name="label">
                <string>connectedTo</string>
            </attr>
        </edge>
        <edge from="n9" to="n10">
            <attr name="label">
                <string>connectedTo</string>
            </attr>
            <attr name="layout">
                <string>268 -23 181 586 555 602 11</string>
            </attr>
        </edge>
        <edge from="n10" to="n10">
            <attr name="label">
                <string>id:Gateway</string>
            </attr>
        </edge>
        <edge from="n10" to="n7">
            <attr name="label">
                <string>connectedTo</string>
            </attr>
        </edge>
        <edge from="n10" to="n3">
            <attr name="label">
                <string>policy</string>
            </attr>
            <attr name="layout">
                <string>360 1 555 602 148 689 11</string>
            </attr>
        </edge>
        <edge from="n10" to="n10">
            <attr name="label">
                <string>type:LocationDigital</string>
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
        <edge from="n5" to="n20">
            <attr name="label">
                <string>connectedTo</string>
            </attr>
        </edge>
        <edge from="n5" to="n12">
            <attr name="label">
                <string>policy</string>
            </attr>
        </edge>
        <edge from="n5" to="n5">
            <attr name="label">
                <string>type:LocationDigital</string>
            </attr>
        </edge>
        <edge from="n7" to="n7">
            <attr name="label">
                <string>type:LocationDigital</string>
            </attr>
        </edge>
        <edge from="n7" to="n5">
            <attr name="label">
                <string>connectedTo</string>
            </attr>
        </edge>
        <edge from="n8" to="n8">
            <attr name="label">
                <string>type:LocationDigital</string>
            </attr>
        </edge>
        <edge from="n8" to="n11">
            <attr name="label">
                <string>connectedTo</string>
            </attr>
        </edge>
        <edge from="n8" to="n12">
            <attr name="label">
                <string>policy</string>
            </attr>
            <attr name="layout">
                <string>493 35 834 607 991 366 11</string>
            </attr>
        </edge>
        <edge from="n8" to="n7">
            <attr name="label">
                <string>connectedTo</string>
            </attr>
        </edge>
        <edge from="n11" to="n13">
            <attr name="label">
                <string>connectedTo</string>
            </attr>
        </edge>
        <edge from="n11" to="n12">
            <attr name="label">
                <string>contains</string>
            </attr>
        </edge>
        <edge from="n11" to="n5">
            <attr name="label">
                <string>connectedTo</string>
            </attr>
        </edge>
        <edge from="n11" to="n11">
            <attr name="label">
                <string>type:LocationDigital</string>
            </attr>
        </edge>
        <edge from="n11" to="n19">
            <attr name="label">
                <string>connectedTo</string>
            </attr>
        </edge>
        <edge from="n12" to="n12">
            <attr name="label">
                <string>type:CredentialDigital</string>
            </attr>
        </edge>
        <edge from="n13" to="n14">
            <attr name="label">
                <string>connectedTo</string>
            </attr>
        </edge>
        <edge from="n13" to="n13">
            <attr name="label">
                <string>type:LocationDigital</string>
            </attr>
        </edge>
        <edge from="n14" to="n15">
            <attr name="label">
                <string>policy</string>
            </attr>
        </edge>
        <edge from="n14" to="n6">
            <attr name="label">
                <string>connectedTo</string>
            </attr>
        </edge>
        <edge from="n14" to="n14">
            <attr name="label">
                <string>type:LocationDigital</string>
            </attr>
        </edge>
        <edge from="n15" to="n15">
            <attr name="label">
                <string>type:CredentialDigital</string>
            </attr>
        </edge>
        <edge from="n16" to="n16">
            <attr name="label">
                <string>type:LocationDigital</string>
            </attr>
        </edge>
        <edge from="n16" to="n13">
            <attr name="label">
                <string>connectedTo</string>
            </attr>
        </edge>
        <edge from="n17" to="n7">
            <attr name="label">
                <string>connectedTo</string>
            </attr>
        </edge>
        <edge from="n17" to="n18">
            <attr name="label">
                <string>policy</string>
            </attr>
        </edge>
        <edge from="n17" to="n17">
            <attr name="label">
                <string>type:LocationDigital</string>
            </attr>
        </edge>
        <edge from="n18" to="n18">
            <attr name="label">
                <string>type:CredentialDigital</string>
            </attr>
        </edge>
        <edge from="n19" to="n19">
            <attr name="label">
                <string>type:LocationDigital</string>
            </attr>
        </edge>
        <edge from="n19" to="n6">
            <attr name="label">
                <string>connectedTo</string>
            </attr>
        </edge>
        <edge from="n19" to="n15">
            <attr name="label">
                <string>policy</string>
            </attr>
        </edge>
        <edge from="n20" to="n6">
            <attr name="label">
                <string>connectedTo</string>
            </attr>
        </edge>
        <edge from="n20" to="n20">
            <attr name="label">
                <string>type:LocationDigital</string>
            </attr>
        </edge>
    </graph>
</gxl>
