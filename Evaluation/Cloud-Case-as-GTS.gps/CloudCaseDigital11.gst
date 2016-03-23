<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<gxl xmlns="http://www.gupro.de/GXL/gxl-1.0.dtd">
    <graph role="graph" edgeids="false" edgemode="directed" id="CloudCaseDigital11">
        <attr name="$version">
            <string>curly</string>
        </attr>
        <node id="n0">
            <attr name="layout">
                <string>475 742 69 24</string>
            </attr>
        </node>
        <node id="n4">
            <attr name="layout">
                <string>528 554 196 24</string>
            </attr>
        </node>
        <node id="n6">
            <attr name="layout">
                <string>530 275 162 24</string>
            </attr>
        </node>
        <node id="n2">
            <attr name="layout">
                <string>546 195 128 24</string>
            </attr>
        </node>
        <node id="n9">
            <attr name="layout">
                <string>706 466 208 24</string>
            </attr>
        </node>
        <node id="n10">
            <attr name="layout">
                <string>518 384 201 24</string>
            </attr>
        </node>
        <node id="n5">
            <attr name="layout">
                <string>802 383 47 24</string>
            </attr>
        </node>
        <node id="n3">
            <attr name="layout">
                <string>950 380 224 24</string>
            </attr>
        </node>
        <node id="n7">
            <attr name="layout">
                <string>784 749 82 24</string>
            </attr>
        </node>
        <node id="n8">
            <attr name="layout">
                <string>787 281 47 24</string>
            </attr>
        </node>
        <node id="n11">
            <attr name="layout">
                <string>947 282 221 24</string>
            </attr>
        </node>
        <node id="n1">
            <attr name="layout">
                <string>972 744 118 24</string>
            </attr>
        </node>
        <node id="n12">
            <attr name="layout">
                <string>286 554 201 24</string>
            </attr>
        </node>
        <node id="n13">
            <attr name="layout">
                <string>237 395 181 24</string>
            </attr>
        </node>
        <node id="n14">
            <attr name="layout">
                <string>223 285 226 24</string>
            </attr>
        </node>
        <node id="n15">
            <attr name="layout">
                <string>302 191 137 24</string>
            </attr>
        </node>
        <node id="n16">
            <attr name="layout">
                <string>110 396 47 24</string>
            </attr>
        </node>
        <node id="n17">
            <attr name="layout">
                <string>103 612 186 24</string>
            </attr>
        </node>
        <node id="n18">
            <attr name="layout">
                <string>748 645 143 24</string>
            </attr>
        </node>
        <node id="n19">
            <attr name="layout">
                <string>121 745 105 24</string>
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
        <edge from="n0" to="n12">
            <attr name="label">
                <string>at</string>
            </attr>
        </edge>
        <edge from="n4" to="n4">
            <attr name="label">
                <string>id:Internet</string>
            </attr>
        </edge>
        <edge from="n4" to="n10">
            <attr name="label">
                <string>connectedTo</string>
            </attr>
            <attr name="layout">
                <string>700 -11 626 566 618 396 11</string>
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
                <string>723 27 626 566 810 478 11</string>
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
        <edge from="n6" to="n8">
            <attr name="label">
                <string>has</string>
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
        <edge from="n9" to="n9">
            <attr name="label">
                <string>id:LaptopOS</string>
            </attr>
        </edge>
        <edge from="n9" to="n3">
            <attr name="label">
                <string>contains</string>
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
                <string>276 9 872 477 618 396 11</string>
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
                <string>392 15 618 396 611 287 11</string>
            </attr>
        </edge>
        <edge from="n10" to="n10">
            <attr name="label">
                <string>type:LocationDigital</string>
            </attr>
        </edge>
        <edge from="n10" to="n5">
            <attr name="label">
                <string>has</string>
            </attr>
        </edge>
        <edge from="n5" to="n5">
            <attr name="label">
                <string>type:Policy</string>
            </attr>
        </edge>
        <edge from="n5" to="n3">
            <attr name="label">
                <string>requires</string>
            </attr>
        </edge>
        <edge from="n3" to="n3">
            <attr name="label">
                <string>id:IPAddress</string>
            </attr>
        </edge>
        <edge from="n3" to="n3">
            <attr name="label">
                <string>type:CredentialDigital</string>
            </attr>
        </edge>
        <edge from="n7" to="n7">
            <attr name="label">
                <string>id:Boss</string>
            </attr>
        </edge>
        <edge from="n7" to="n11">
            <attr name="label">
                <string>has</string>
            </attr>
        </edge>
        <edge from="n7" to="n7">
            <attr name="label">
                <string>type:Role</string>
            </attr>
        </edge>
        <edge from="n7" to="n3">
            <attr name="label">
                <string>has</string>
            </attr>
            <attr name="layout">
                <string>500 0 825 761 920 755 1072 392 11</string>
            </attr>
        </edge>
        <edge from="n7" to="n17">
            <attr name="label">
                <string>has</string>
            </attr>
            <attr name="layout">
                <string>500 0 825 761 846 653 196 624 11</string>
            </attr>
        </edge>
        <edge from="n8" to="n11">
            <attr name="label">
                <string>requires</string>
            </attr>
        </edge>
        <edge from="n8" to="n8">
            <attr name="label">
                <string>type:Policy</string>
            </attr>
        </edge>
        <edge from="n11" to="n11">
            <attr name="label">
                <string>id:Password</string>
            </attr>
        </edge>
        <edge from="n11" to="n11">
            <attr name="label">
                <string>type:CredentialDigital</string>
            </attr>
        </edge>
        <edge from="n1" to="n1">
            <attr name="label">
                <string>id:Employee</string>
            </attr>
        </edge>
        <edge from="n1" to="n17">
            <attr name="label">
                <string>has</string>
            </attr>
        </edge>
        <edge from="n1" to="n11">
            <attr name="label">
                <string>has</string>
            </attr>
        </edge>
        <edge from="n1" to="n1">
            <attr name="label">
                <string>type:Role</string>
            </attr>
        </edge>
        <edge from="n12" to="n12">
            <attr name="label">
                <string>id:Outside</string>
            </attr>
        </edge>
        <edge from="n12" to="n18">
            <attr name="label">
                <string>contains</string>
            </attr>
        </edge>
        <edge from="n12" to="n13">
            <attr name="label">
                <string>connectedTo</string>
            </attr>
        </edge>
        <edge from="n12" to="n12">
            <attr name="label">
                <string>type:LocationPhysical</string>
            </attr>
        </edge>
        <edge from="n13" to="n13">
            <attr name="label">
                <string>id:Door</string>
            </attr>
        </edge>
        <edge from="n13" to="n14">
            <attr name="label">
                <string>connectedTo</string>
            </attr>
        </edge>
        <edge from="n13" to="n13">
            <attr name="label">
                <string>type:LocationPhysical</string>
            </attr>
        </edge>
        <edge from="n13" to="n16">
            <attr name="label">
                <string>has</string>
            </attr>
        </edge>
        <edge from="n14" to="n14">
            <attr name="label">
                <string>id:Datacenter</string>
            </attr>
        </edge>
        <edge from="n14" to="n14">
            <attr name="label">
                <string>type:LocationPhysical</string>
            </attr>
        </edge>
        <edge from="n14" to="n15">
            <attr name="label">
                <string>contains</string>
            </attr>
        </edge>
        <edge from="n15" to="n15">
            <attr name="label">
                <string>id:Server</string>
            </attr>
        </edge>
        <edge from="n15" to="n6">
            <attr name="label">
                <string>contains</string>
            </attr>
        </edge>
        <edge from="n15" to="n15">
            <attr name="label">
                <string>type:AssetItem</string>
            </attr>
        </edge>
        <edge from="n16" to="n16">
            <attr name="label">
                <string>type:Policy</string>
            </attr>
        </edge>
        <edge from="n16" to="n17">
            <attr name="label">
                <string>requires</string>
            </attr>
        </edge>
        <edge from="n17" to="n17">
            <attr name="label">
                <string>id:Key</string>
            </attr>
        </edge>
        <edge from="n17" to="n17">
            <attr name="label">
                <string>type:CredentialPhysical</string>
            </attr>
        </edge>
        <edge from="n18" to="n18">
            <attr name="label">
                <string>id:Laptop</string>
            </attr>
        </edge>
        <edge from="n18" to="n18">
            <attr name="label">
                <string>type:AssetItem</string>
            </attr>
        </edge>
        <edge from="n18" to="n9">
            <attr name="label">
                <string>contains</string>
            </attr>
        </edge>
        <edge from="n19" to="n19">
            <attr name="label">
                <string>id:Cleaner</string>
            </attr>
        </edge>
        <edge from="n19" to="n17">
            <attr name="label">
                <string>has</string>
            </attr>
        </edge>
        <edge from="n19" to="n19">
            <attr name="label">
                <string>type:Role</string>
            </attr>
        </edge>
    </graph>
</gxl>
