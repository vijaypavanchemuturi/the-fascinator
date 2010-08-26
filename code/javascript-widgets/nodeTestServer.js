
// This is a simple (node.js) test server script

// Requires 'formidable' - http://github.com/felixge/node-formidable
// Requires 'json-template' - http://json-template.googlecode.com/files/json-template.js
//    Note: requires the following line added to the bottom of this file:
//          " exports.jsontemplate = jsontemplate; "
//    {key}  {.section key}..ifLike..{.or}..elseLike..{.end}  {.repeated section key}..loop..{.end}
//    @ - (current item) can be used in replace of the 'key' argument for the current 'key'

var log=console.log;
var exit=process.exit;
var sys=require("sys");
var fs=require("fs");
var path=require("path");
var http=require("http");
var url=require("url");
var formidable;
var jtemp;
var server, port=9123;
var htmlUploadForm;
try{
  formidable=require("formidable");
}catch(e){
  log("Requires 'formidable' - avaialble from: http://github.com/felixge/node-formidable");
  exit();
}
try{
  jtemp=require("json-template").jsontemplate;
}catch(e){
  log("Requires 'json-template' - avaiable from: http://json-template.googlecode.com/files/json-template.js");
  exit();
}

htmlUploadForm="<form action='/upload' method='POST' enctype='multipart/form-data'>" +
" <input type='file' name='upload-file'/> <p><input type='text' name='testtext' value='testing [123]'/></p>" +
" <input type='checkbox' value='1' name='ajax'/>AJAX &#160; " +
" <input type='submit' value='upload'/>" +
"</form>";

function each(obj, func){ 
  for(var k in obj){ func(k, obj[k]); }
}

server=http.createServer(function(req, res){
  var urlInfo = url.parse(req.url);
  switch(urlInfo.pathname){
    case "/upload": fileUpload(req, res); break;
    case "/": htmlOutput(res, htmlUploadForm); break;
    default: show404(res); break;
  }
});
server.listen(port);
log("Serving on http://localhost:"+port+"/");
fs.mkdir("temp", 0777);


function fileUpload(req, res){
  var html, t, ajax=false;
  var iform = new formidable.IncomingForm();
  iform.uploadDir = "temp";
  iform.keepExtensions = true;
  iform.parse(req, function(err, fields, files){
    each(files, function(k, v){
      v.stats = fs.statSync(v.path);
    });
    if(fields.ajax){
      html = JSON.stringify({fields:fields, files:files});
    }else{
      t = jtemp.Template("<div><p>File upload:</p> Fields:<pre>{fields}</pre> Files:<pre>{files}</pre><a href='/'>back</a></div>");
      html=t.expand({fields:JSON.stringify(fields), files:JSON.stringify(files)});
    }
    htmlOutput(res, html);
  });
}

function htmlOutput(res, html){
  res.writeHead(200, {"content-type":"text/html"});
  res.write(html);
  res.end();
}

function show404(res){
  res.writeHead(404, {"content-type":"text/plain"});
  res.write("Not found!");
  res.end();
}




