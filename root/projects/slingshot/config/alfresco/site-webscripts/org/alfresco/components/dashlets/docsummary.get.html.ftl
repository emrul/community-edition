<@markup id="css" >
   <#-- CSS Dependencies -->
   <@link rel="stylesheet" type="text/css" href="${url.context}/res/components/dashlets/docsummary.css" group="dashlets"/>
</@>

<@markup id="js">
   <#-- JavaScript Dependencies -->
   <@script type="text/javascript" src="${url.context}/res/components/dashlets/docsummary.js" group="dashlets"/>
</@>

<@markup id="pre">
   <#-- No pre-instantiation JavaScript required -->
</@>

<@markup id="widgets">
   <@createWidgets group="dashlets"/>
</@>

<@markup id="post">
   <#-- No post-instantiation JavaScript required -->
</@>

<@markup id="html">
   <@uniqueIdDiv>
      <#assign id = args.htmlid>
      <#assign jsid = args.htmlid?js_string>
      <#assign prefSimpleView = preferences.simpleView!true>
      <div class="dashlet docsummary">
         <div class="title">${msg("header")}</div>
         <div class="toolbar flat-button">
            <div id="${id}-simpleDetailed" class="align-right simple-detailed yui-buttongroup inline">
               <span class="yui-button yui-radio-button simple-view<#if prefSimpleView> yui-button-checked yui-radio-button-checked</#if>">
                  <span class="first-child">
                     <button type="button" tabindex="0" title="${msg("button.view.simple")}"></button>
                  </span>
               </span>
               <span class="yui-button yui-radio-button detailed-view<#if !prefSimpleView> yui-button-checked yui-radio-button-checked</#if>">
                  <span class="first-child">
                     <button type="button" tabindex="0" title="${msg("button.view.detailed")}"></button>
                  </span>
               </span>
            </div>
            <div class="clear"></div>
         </div>
         <div class="body scrollableList" <#if args.height??>style="height: ${args.height}px;"</#if>>
            <div id="${id}-documents"></div>
         </div>
      </div>
   </@>
</@>