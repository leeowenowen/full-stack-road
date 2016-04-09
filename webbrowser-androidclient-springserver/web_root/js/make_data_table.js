//TODO: create with createElement, not string concat
function makeTd(content, td_class) {
    return "<td class= " + td_class + ">" + content + "</td>";
}
function makeHeaderTr(headers) {
    var ret = "";
    for (var i = 0; i < headers.length; i++) {
        ret = ret + makeTd(headers[i], "td_class_header")
    }
    ret = "<tr class='tr_class_header'>" + ret + "</tr>"
    return ret;
}
function makeHeader(headers) {
    return "<thead class='thead_class'>" + makeHeaderTr(headers) + "</thead>";
}
function makeBodyTr(keys, row_obj) {
    var ret = "";
    for (var i = 0; i < keys.length; i++) {
        ret = ret + makeTd(row_obj[keys[i]], "td_class_body")
    }
    ret = "<tr class='tr_class_body'>" + ret + "</tr>"
    return ret;
}
function makeBody(keys, contents) {
    var ret = "";
    for (var i = 0; i < contents.length; i++) {
        ret = ret + makeBodyTr(keys, contents[i])
    }
    ret = "<tbody class='tbody_class'>" + ret + "</tbody>"
    return ret;

}
function makeTable(headers, keys, configs) {
    var ret = "";
    ret = ret + makeHeader(headers);
    ret = ret + makeBody(keys, configs);
    ret = "<table class='table_class' id='table_id'>" + ret + "</table>";
    return ret;
}

