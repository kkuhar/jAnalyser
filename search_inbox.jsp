<%--
/*
**
** Confidential and Proprietary property of Infotehna Group LLC
** (c) Copyright, Infotehna Group LLC 2002-2011
** All rights reserved.
** May be used only in accordance with the terms and conditions of the
** license agreement governing use of Infotehna software 
** between you and Infotehna or Infotehna's authorized reseller. 
** Not to be changed without prior written permission of Infotehna.				
** Any other use is strictly prohibited.
**
** $Revision: 1.42 $ 
** $Date: 2013/12/06 10:27:57 $
** $Author: goranh $
**
*/
--%>
<%@ page language="java" errorPage="../error/start.jsp" %>

<%@ page import="com.documentum.fc.client.*" %>
<%@ page import="com.documentum.fc.common.*" %>
<%@ page import="com.infotehna.utils.*" %>
<%@ page import="com.infotehna.myprocess.*" %>
<%@ page import="com.infotehna.myprocess.beans.*" %>
<%@ page import="com.infotehna.myprocess.ui.*" %>
<%@ page import="com.infotehna.myprocess.ui.beans.*" %>
<%@ page import="com.infotehna.myprocess.search.*" %>
<%@ page import="com.infotehna.cache.*" %>
<%@ page import="com.infotehna.cache.beans.*" %>
<%@ page import="com.infotehna.myprocess.profiling.*" %>
<%@ page import="com.infotehna.myprocess.documentum.*" %>



<%@ include file="../include/context.jsp" %>
<%@ include file="../include/auth.jsp" %>

<%
	Logger.getLogger().trace("[search_inbox.jsp] START");
	long startTime = System.currentTimeMillis();
	
	String preContainerFilePath = "?preContainerFilePath=" + contextPath + jspPath + "info_center/search_inbox.jsp";
		
	ArrayList result = null;
	
	String language = uiStrings.getLanguage();
	PersonalizationData pdManager = PersonalizationData.getInstance(language, dfcSession.getUser(""));
	PersonalizationDataBean pdb = pdManager.getPersonalizationDataBeanById(dfcSession.getUser("").getUserOSName());

	String filterBy = request.getParameter("filterBy");
	
	if (filterBy == null || filterBy.equals("")) {
		filterBy = (String)session.getAttribute("filterBy");
		request.setAttribute("filterBy", filterBy);
	}
	
	if (filterBy == null || filterBy.equals("")) 
		filterBy = "ALL";
		
	session.setAttribute("filterBy", filterBy);
	
	String destination = request.getParameter("destination");
	if (destination == null || destination.equals("")) destination = "inbox";
	
	String forPrint = request.getParameter("forPrint");
	if (forPrint == null || forPrint.equals("")) forPrint = "";
	
	String from = request.getParameter("fromField");
	if (from == null || from.equals("")) from = "";
	
	//isv Task 1936
	String to = request.getParameter("toField");
	if (to == null || to.equals("")) to = "";
	
	String subject = request.getParameter("subjectField");
	if (subject == null || subject.equals("")) subject = "";
	
	String msg = request.getParameter("msgField");
	if (msg == null || msg.equals("")) msg = "";
	
	String dateBefore = request.getParameter("dateBefore");
	if (dateBefore == null || dateBefore.equals("")) dateBefore = null;
	
	String dateAfter = request.getParameter("dateAfter");
	if (dateAfter == null || dateAfter.equals("")) dateAfter = null;
	
	String document = request.getParameter("documentMsgField");
	if (document == null || document.equals("")) document = "";
	
	//hli
	String selectedObjectId = request.getParameter("selectedObjectId");
  	if (selectedObjectId == null) {
		selectedObjectId = "";
  	}
  	Logger.getLogger().debug("selectedObjectId = " + selectedObjectId);
  	
  	String profileId = request.getParameter("profileId");
  	if (profileId == null) {
  		profileId = "";
  	}
  	Logger.getLogger().debug("profileId = " + profileId);
  	
  	String allIdsStr = "";
  	String allTasks = "";
  	String allProfilesStr = "";
  	if (!selectedObjectId.equals("") && !selectedObjectId.startsWith("capa_requests")) {
  		allIdsStr = "'" + selectedObjectId + "',";
  		ArrayList<String> allIds = new ArrayList<String>();
  		MyProcessUtils.getAllVDComponents(selectedObjectId, allIds, true, dfcSession);
  		for (int i = 0; i < allIds.size(); i++) {
  			allIdsStr += "'" + allIds.get(i) + "'";
  			if (i < allIds.size() - 1) {
  				allIdsStr += ",";
  			}
  		}  		
  	}
  	else if (selectedObjectId.startsWith("capa_requests")) {
  		if (profileId.equals("")) {
  			allProfilesStr = "'cd_audit_bin', 'cd_com_bin', 'cd_deviation', 'cd_incident'";
  		}
  		else if (profileId.indexOf('~') != -1) {
  			String[] profileIds = profileId.split("~");
  			for (int i = 0; i < profileIds.length; i++) {
  				allProfilesStr += "'" + profileIds[i] + "'";
  				if (i < profileIds.length - 1) {
  					allProfilesStr += ",";
  				}
  			}
  		}
  		else {
  			allProfilesStr = "'" + profileId + "'";
  		}
  	}
  	Logger.getLogger().debug("allIdsStr = " + allIdsStr);
  	Logger.getLogger().debug("allProfilesStr = " + allProfilesStr);
  	//hli
	
	String currentDocbaseId = lm.getDocbaseId();
	
	Persons pManager = Persons.getInstance(language);
	PersonBean pb = pManager.getPersonByName(lm.getUserName(), lm.getDocbaseId());
	
	//isv Task 1787
	String folderId = request.getParameter("folderId"); // folder id
	String searchActiveFolder = request.getParameter("searchActiveFolder");
	String activeFolderId = "";
	
	IDfFolder inboxFolder = (IDfFolder) dfcSession.getObjectByPath(pb.getDefaultFolder() + "/Inbox"); // default Inbox folder
	
	
	String inboxId = (inboxFolder != null) ? inboxFolder.getObjectId().toString() : ""; //default Inbox folder
	
	String sharedUserName = request.getParameter("sharedUserName");
	boolean sharedUserFlag = false;
	
	String sharedUserId = request.getParameter("sharedUserId");		
	if (sharedUserId != null && !sharedUserId.equals("") && !sharedUserId.equals("null")) {
		PersonBean sharedpb = pManager.getPersonById(sharedUserId);	
		if (sharedpb != null) {
			sharedUserName = sharedpb.getName();
			
			IDfFolder sharedInboxFolder = (IDfFolder) dfcSession.getObjectByPath(sharedpb.getDefaultFolder() + "/Inbox");
			inboxId = (sharedInboxFolder != null) ? sharedInboxFolder.getObjectId().toString() : "";
		}	else  {//isv-task 2190
			if (forPrint.equals("true")){
				folderId = sharedUserId;
			}
		}
	}
	
	//isv task 1787 begin 
	if (folderId != null && !folderId.equals ("") && !inboxId.equals("") && !folderId.equals("sent_items")&& !folderId.equals("workflows")) {
			
			if(!folderId.equals(inboxId) ) {
				activeFolderId = folderId;
			} else {
				activeFolderId = inboxId;
			}
		
		} else if (!inboxId.equals("")){
			
			activeFolderId = inboxId;
		}
	
	ArrayList subfolderIds = new ArrayList();
	DfQuery querySubFol = new MpDfQuery();
	String dqlSubFol = "";
	IDfCollection subFolderColl = null;
	folderId = activeFolderId;
	DfQuery queryFol = new MpDfQuery();
	String dqlFol = "";
	IDfCollection folderColl = null;
	ArrayList folderIds = new ArrayList ();
	folderIds.add(activeFolderId);
	
	if ((searchActiveFolder != null && !searchActiveFolder.equals("")) || (forPrint.equals("true"))){//isv-task 2190
		if (searchActiveFolder == null) searchActiveFolder = "false";
		
		if ((searchActiveFolder.equals("false") || forPrint.equals("true") ) && activeFolderId != null && !activeFolderId.equals("")){//isv-task 2190
		
			try {
			dqlFol = "SELECT * FROM dm_folder WHERE FOLDER (ID('"+ activeFolderId+"'), DESCEND)";
			queryFol.setDQL(dqlFol);
		 	folderColl = queryFol.execute(dfcSession, DfQuery.DF_EXECREAD_QUERY);
			
			if (folderColl != null){
				while (folderColl.next()){
					String objId = folderColl.getString("r_object_id");
					folderIds.add((String)objId);
					}
				}
			
			} catch (Exception ex) {
				Logger.getLogger().error("Error while getting subfolders: " + ex);
			} finally {
				if (folderColl != null)
					folderColl.close();
			}
			
			
		}
		
	}
	//isv Task 1787 end
	
	// isv Task 1811	
		try {
			dqlSubFol = "SELECT * FROM dm_folder WHERE FOLDER (ID('"+ inboxId+"'), DESCEND)";
			querySubFol.setDQL(dqlSubFol);
		 	subFolderColl = querySubFol.execute(dfcSession, DfQuery.DF_EXECREAD_QUERY);
			
			if (subFolderColl != null){
				while (subFolderColl.next()){
					String objId = subFolderColl.getString("r_object_id");
					subfolderIds.add((String)objId);
					}
				}
			} catch (Exception ex) {
				Logger.getLogger().error("Error while getting subfolders: " + ex);
			} finally {
				if (subFolderColl != null)
					subFolderColl.close();
			}
		
	
	//isv Task 1811 end
	
	
	if (sharedUserName != null && !sharedUserName.equals(""))
		sharedUserFlag = true;
	
	if (!sharedUserFlag) sharedUserName = dfcSession.getLoginUserName();
	
	Logger.getLogger().info("[search_inbox.jsp] : sharedUserName = " + sharedUserName);
	

	try {
		Logger.getLogger().info("[search_inbox.jsp] : Starting inbox/sent items search...");
	
		IDfDocbaseMap docbaseMap = DocbaseUtility.getDocbaseMap();

		IDfSession inboxDfcSession = null;
		ArrayList allRows = new ArrayList();

		for(int i = 0; i < docbaseMap.getDocbaseCount(); i++)
		{
			String docbaseId = docbaseMap.getDocbaseId(i);
			
			if (sharedUserFlag) {
				/*IDfSession adminSession = DocbaseUtility.getAdminSessionByDocbaseId(docbaseId);
				inboxDfcSession = adminSession;*/
				inboxDfcSession = (IDfSession) lm.getActiveSession(docbaseId);				
			} else {
				inboxDfcSession = (IDfSession) lm.getActiveSession(docbaseId);
			}
			

			if(inboxDfcSession == null || (inboxDfcSession != null && !inboxDfcSession.isConnected())) {
				if (lm.loginToDocbase(docbaseId)) {
					inboxDfcSession = (IDfSession) lm.getActiveSession(docbaseId);
				} else {
					continue;
				}
			}
			

			if(inboxDfcSession != null) {
				if(inboxDfcSession.isConnected()) {
					DfQuery query = new MpDfQuery();
					
					String dql = "";
					String additionalWhereClauses = "";
					ActionsUtility au = new ActionsUtility (inboxDfcSession, language);
					FullTextSearchBean ftsb = new FullTextSearchBean();
					String returnMaxItems = "";
					if (destination.equals("inbox") || destination.equals("sent") || destination.equals("inbox_express") || destination.equals("sent_express")) {
						String performerName = "";
						if (destination.equals("inbox") || destination.equals("inbox_express")){
							additionalWhereClauses = "(name in ('" + sharedUserName + "' ";//dma-task 3043
							//additionalWhereClauses = "name = '" + sharedUserName + "' or name in (select group_name from dm_group where any users_names = '" + sharedUserName + "')";
							try {
								//isv Task
								ProfileUtil pu = new ProfileUtil(language,inboxDfcSession);
								ArrayList userGroups  =  pu.getAllGroupsForUser(sharedUserName);
								//isv Task end
								/*UserData ud = ActiveUsersData.getInstance().getUserData(sharedUserName, inboxDfcSession.getDocbaseName());
								ArrayList userGroups = ud.getUserGroups();*/
								//pmi
								int orStatement = 0;
								if (userGroups.size() > 490) {
									orStatement = userGroups.size()/490;
								}
								for(int j = 0; userGroups != null && j < userGroups.size(); j++) {
									if (orStatement > 0) {
										for (int jj = 1 ; jj <= orStatement; jj++) {
											if (j == (jj*490)) {
												additionalWhereClauses += ") or name in (" + "'" + userGroups.get(j) + "'";
											}
											else {
												additionalWhereClauses += ",'" + userGroups.get(j) + "'";
											}
										}
									}
									else {
										additionalWhereClauses += ",'" + userGroups.get(j) + "'";
									}
								}
								
								
							} catch(Exception e){
							}
							
							additionalWhereClauses += "))";//dma-task 3043
														
							if (!from.equals("")) {
								additionalWhereClauses += " and sent_by like '%" + from + "%'";
								//performerName = "and dmi_workitem.r_performer_name = '" + inboxDfcSession.getLoginUserName() + "'";
								performerName = "and dmi_workitem.r_performer_name = '" + sharedUserName + "'";
							}
							//isv Task 1936
							if (!to.equals("")) {
								additionalWhereClauses += " and name like '%" + to + "%'";
								//performerName = "and dmi_workitem.r_performer_name = '" + inboxDfcSession.getLoginUserName() + "'";
								performerName = "and dmi_workitem.r_performer_name = '" + sharedUserName + "'";
							}
							
							//hli
							if (!allIdsStr.equals("")) {						
								additionalWhereClauses += " and (item_id in (select dmi_workitem.r_object_id from dmi_workitem, dmi_package where dmi_package.r_act_seqno = dmi_workitem.r_act_seqno and dmi_package.r_workflow_id = dmi_workitem.r_workflow_id and any dmi_package.r_component_id in (" + allIdsStr + ")) OR item_id in (" + allIdsStr + "))";
							}
							if (selectedObjectId.equals("capa_requests")) {
								additionalWhereClauses += " and router_id in (select r_object_id from dm_workflow where any instructions is not null)";
								additionalWhereClauses += " and item_id in (select dmi_workitem.r_object_id from dmi_workitem, dmi_package where dmi_package.r_act_seqno = dmi_workitem.r_act_seqno and dmi_package.r_workflow_id = dmi_workitem.r_workflow_id and any dmi_package.r_component_id in (select doc.r_object_id from it_pharma_doc doc, it_sysobject sys where doc.r_object_id = sys.mp_object_id and sys.mp_profile_id in (" + allProfilesStr + ")))";
							}
							//hli
						}
						
						if (destination.equals("sent") || destination.equals("sent_express")){
							//hli
							if (!allIdsStr.equals("")) {						
								additionalWhereClauses += " (item_id in (select dmi_workitem.r_object_id from dmi_workitem, dmi_package where dmi_package.r_act_seqno = dmi_workitem.r_act_seqno and dmi_package.r_workflow_id = dmi_workitem.r_workflow_id and any dmi_package.r_component_id in (" + allIdsStr + ")) OR item_id in (" + allIdsStr + "))";
							}
							else if (selectedObjectId.equals("capa_requests_all")) {
								additionalWhereClauses += " item_id in (select dmi_workitem.r_object_id from dmi_workitem, dmi_package where dmi_package.r_act_seqno = dmi_workitem.r_act_seqno and dmi_package.r_workflow_id = dmi_workitem.r_workflow_id and any dmi_package.r_component_id in (select doc.r_object_id from it_pharma_doc doc, it_sysobject sys where doc.r_object_id = sys.mp_object_id and sys.mp_profile_id = 'cd_deviation_vd'))";
							}
							else {
								additionalWhereClauses = "sent_by = '" + sharedUserName + "'";
								
								if (pdb.getInfoCenterSettings().getHideNotificationsToDmFullTextIndexUser()) {
									additionalWhereClauses += " and name <> 'dm_fulltext_index_user'";
								}
								
								if (!from.equals("")) {
									additionalWhereClauses += " and name like '%" + from + "%'";
									performerName = "and dmi_workitem.r_performer_name = '" + from + "'";
								}
							}
							//hli
						}
						
						if (filterBy.equals("TASK")){
							additionalWhereClauses += " and router_id != '0000000000000000'";
						}
						else if (filterBy.equals("NOTIFICATION")){
							additionalWhereClauses += " and router_id = '0000000000000000'";
						}
						
						additionalWhereClauses += " and event != 'dm_pseudocompletedworkitem' and event != 'dm_wf_autodelegate_failure'";//dma-task 3180
					
						if (!subject.equals("")){
							additionalWhereClauses += " and (item_name like '%" + subject + "%' or task_name like '%" + subject + "%')";
						}
						if (!msg.equals("")){
							additionalWhereClauses += " and message like '%" + msg + "%'";
						}
						if (dateBefore != null){
							additionalWhereClauses += " and date_sent <= DATE('" + dateBefore + "', 'yyyy/mm/dd')";
						}
						if (dateAfter != null){
							additionalWhereClauses += " and date_sent >= DATE('" + dateAfter + "', 'yyyy/mm/dd')";
						}
						
						if (!document.equals("")) {
							boolean isFullTextActive = ftsb.checkConfigurationParametersForFullText("info_center");
							if (isFullTextActive == true){
								ftsb.setFulltextSearchString("'\"*" + document + "*\"'");
								returnMaxItems = " enable (return_top " + ftsb.getMaxItemsFullText() + ")";
							}
							if (!ftsb.getSearchCondition().equals ("")){
								additionalWhereClauses += " and ((item_type <> 'manual' and item_id in (select r_object_id from dm_sysobject(all) " + ftsb.getSearchCondition() + ")) or";
								additionalWhereClauses += " (item_type = 'manual' and item_id in (select dmi_workitem.r_object_id from dmi_workitem, dmi_package where dmi_workitem.r_workflow_id = dmi_package.r_workflow_id " + performerName + " and dmi_workitem.r_runtime_state <> 2 and any dmi_package.r_component_id in (select r_object_id from dm_sysobject(all) "+ ftsb.getSearchCondition() +"))))";
							}
							else {
								additionalWhereClauses += " and ((item_type <> 'manual' and item_id in (select r_object_id from dm_sysobject(all) where object_name like '%" + document + "%')) or";
								additionalWhereClauses += " (item_type = 'manual' and item_id in (select dmi_workitem.r_object_id from dmi_workitem, dmi_package where dmi_workitem.r_workflow_id = dmi_package.r_workflow_id " + performerName + " and dmi_workitem.r_runtime_state <> 2 and any dmi_package.r_component_id in (select r_object_id from dm_sysobject(all) where object_name like '%" + document +"%'))))";
							}
							
							
						//	additionalWhereClauses += " and ((item_type <> 'manual' and item_id in (select r_object_id from dm_sysobject(all) where object_name like '%" + document + "%')) or";
						//	additionalWhereClauses += " (item_type = 'manual' and item_id in (select dmi_workitem.r_object_id from dmi_workitem, dmi_package where dmi_workitem.r_workflow_id = dmi_package.r_workflow_id " + performerName + " and dmi_workitem.r_runtime_state <> 2 and any dmi_package.r_component_id in (select r_object_id from dm_sysobject(all) where object_name like '%" + document +"%'))))";
						
						//	!! SEARCH DOCUMENT CONTAINS -> SEARCH TOPIC !! ALIMS
						//	!! SEARCH DOCUMENT CONTAINS replaces some characters (-) with blanks !!
						//	additionalWhereClauses += " and ((item_type <> 'manual' and item_id in (select r_object_id from dm_sysobject(all) search document contains '" + document + "')) or";
						//	additionalWhereClauses += " and ((item_type <> 'manual' and item_id in (select r_object_id from dm_sysobject(all) search topic '" + document + "')) or";
							
						// !! SEARCH DOCUMENT CONTAINS -> SEARCH TOPIC !! ALIMS
						// !! SEARCH DOCUMENT CONTAINS replaces some characters (-) with blanks !!
						//	additionalWhereClauses += " (item_type = 'manual' and item_id in (select dmi_workitem.r_object_id from dmi_workitem, dmi_package where dmi_workitem.r_workflow_id = dmi_package.r_workflow_id " + performerName + " and dmi_workitem.r_runtime_state <> 2 and any dmi_package.r_component_id in (select r_object_id from dm_sysobject(all) search document contains '" + document +"'))))";
						//	additionalWhereClauses += " (item_type = 'manual' and item_id in (select dmi_workitem.r_object_id from dmi_workitem, dmi_package where dmi_workitem.r_workflow_id = dmi_package.r_workflow_id " + performerName + " and dmi_workitem.r_runtime_state <> 2 and any dmi_package.r_component_id in (select r_object_id from dm_sysobject(all) search topic '" + document + "'))))";
						}
						//isv Task 1787 begin
						
					if (folderIds != null){
						for (int k = 0; k <folderIds.size(); k++){
							String tempFolderId = (String) folderIds.get(k);
							String additionalQuery = "";
							String searchAllTasksForUser = "";
							//dpa - Task 382
							Logger.getLogger().debug("[search_inbox.jsp] : destination = " + destination + ", inboxId = " + inboxId + ", tempFolderId = " + tempFolderId);
							if (!destination.equals("sent") && !destination.equals("sent_express") && !tempFolderId.equals(inboxId)) {
								additionalQuery = " and stamp IN (select stamp from dmi_queue_item, dm_queue, dm_relation where relation_name = 'IT_INBOX_FOLDER' and dm_queue.stamp  = dmi_queue_item.r_object_id and dmi_queue_item.r_object_id = dm_relation.parent_id and child_id = '" + tempFolderId + "')";
							}
							else if (!destination.equals("sent") && !destination.equals("sent_express") && !inboxId.equals("")&& tempFolderId.equals(inboxId)){
								additionalQuery = " and stamp NOT IN (select stamp from dmi_queue_item, dm_queue, dm_relation where relation_name = 'IT_INBOX_FOLDER' and dm_queue.stamp  = dmi_queue_item.r_object_id and dmi_queue_item.r_object_id = dm_relation.parent_id)";
								//dma-task 3198 removed additional query elements
							}
							//isv Task 1811 end
							//dpa - Task 382
							
							String dqlTemp = "select * from dm_queue where " + additionalWhereClauses + additionalQuery + searchAllTasksForUser + "order by 5 desc, 7" + returnMaxItems;
							
							query.setDQL(dqlTemp);
							
							IDfCollection rowsTemp = null;
							ArrayList allRowsTemp = new ArrayList();
							try {
								rowsTemp = query.execute(inboxDfcSession, DfQuery.DF_EXECREAD_QUERY);
									
								while(rowsTemp.next()) {
									allRowsTemp.add(rowsTemp.getTypedObject());
								}
								allRows.addAll(allRowsTemp);
								
									
							} catch(Exception e) {
								Logger.getLogger().error("Error while getting queue items: " + e);
							} finally {
								if(rowsTemp != null){
									rowsTemp.close();
								}
							}
								
						}
					}	
					//isv task 1787 end
					} else if (destination.equals("workflows") || destination.equals("workflows_express") || destination.equals("manager")) {
						if (!from.equals("")) additionalWhereClauses = " and object_name like '%" + from + "%'";
						
						if (dateBefore != null) additionalWhereClauses += " and r_start_date <= DATE('" + dateBefore + "', 'yyyy/mm/dd')";
						if (dateAfter != null) additionalWhereClauses += " and r_start_date >= DATE('" + dateAfter + "', 'yyyy/mm/dd')";
						
						// runtime state
						if (!subject.equals("")) additionalWhereClauses += " and r_runtime_state = " + subject;
						
						if (!document.equals("")) {
							additionalWhereClauses += " and r_object_id in (select r_workflow_id from dmi_package where any r_component_id in (select r_object_id from dm_sysobject(all) where object_name like '%" + document +"%'))";
						}
						
						if (destination.equals("workflows") || destination.equals("workflows_express")) 
							dql = "select r_object_id, object_name, r_runtime_state, r_start_date, r_creator_name, supervisor_name from dm_workflow where supervisor_name = '" + sharedUserName + "' " + additionalWhereClauses + " order by r_start_date desc, object_name";

						if (destination.equals("manager"))
							dql = "select r_object_id, object_name, r_runtime_state, r_start_date, r_creator_name, supervisor_name from dm_workflow where supervisor_name in (select i_all_users_names from dm_group where group_name = '" + inboxDfcSession.getUser("").getUserGroupName() + "') " + additionalWhereClauses + " order by r_start_date desc, object_name";
		
					}
					
					
					
					Logger.getLogger().info("[search_inbox.jsp] : target docbase: " + inboxDfcSession.getDocbaseName());
					if ((destination.equals("workflows") || destination.equals("workflows_express") || destination.equals("manager"))){
					
					query.setDQL(dql);

					IDfCollection rows = null;
					int j = 0;
					try {
						rows = query.execute(inboxDfcSession, DfQuery.DF_EXECREAD_QUERY);
						while(rows.next()) {
							allRows.add(rows.getTypedObject());
						}
					} catch(Exception e) {
	    				Logger.getLogger().error("Error while getting queue items: " + e);
					} finally {
						if(rows != null)
							rows.close();
					}
					}
				}
			}

		}
		Logger.getLogger().debug("Collected number: " + allRows.size());

		// inner class used for sorting
        class SortItemsComparator implements Comparator {
			private String attrName = "";
			private String sortDirection = "";
			private LoginManager lm = null;
			private ResourceStrings uiStrings = null;
			
			public SortItemsComparator() {
				this.attrName = "sent";
				this.sortDirection = "asc";
				this.lm = null;
				this.uiStrings = null;
			}
			
			public SortItemsComparator(String attrName, String sortDirection, LoginManager lm, ResourceStrings uiStrings) {
				this.attrName = attrName;
				this.sortDirection = sortDirection;
				this.lm = lm;
				this.uiStrings = uiStrings;
			}
			
            public int compare(Object o1, Object o2) {
				int tmpResult = 0;
				String realAttrName = "";
				
				// ********************* integer compare / priority *********************************************/
				if (this.attrName.equals("priority")) {
					realAttrName = "priority";
					try {
						int int1 = ((IDfTypedObject)o1).getInt(realAttrName);
						int int2 = ((IDfTypedObject)o2).getInt(realAttrName);
						
						if (int1 == int2) tmpResult = 0;
						if (int1 < int2) tmpResult = -1;
						if (int1 > int2) tmpResult = 1;
					
					} catch(Exception e) {
						Logger.getLogger().error("[search_inbox.jsp] Integer compare error! attrName:  " + this.attrName + " sortDirection: " + this.sortDirection + " Excetpion: " + e);
						return 0;
					}
				// ********************* date compare / sent, date_sent, due_date, start_date *******************************************************/
				} else if (this.attrName.equals("sent") || this.attrName.equals("date_sent") || this.attrName.equals("due_date") || this.attrName.equals("start_date")) {
					if (this.attrName.equals("sent") || this.attrName.equals("date_sent")) realAttrName = "date_sent";
					if (this.attrName.equals("due_date")) realAttrName = "due_date";
					if (this.attrName.equals("start_date")) realAttrName = "r_start_date";
					try {
						IDfTime time1 = ((IDfTypedObject)o1).getTime(realAttrName);
						IDfTime time2 = ((IDfTypedObject)o2).getTime(realAttrName);

						tmpResult = time1.compareTo(time2);
					
					} catch(Exception e) {
						Logger.getLogger().error("[search_inbox.jsp] Date compare error! attrName:  " + this.attrName + " sortDirection: " + this.sortDirection + " Excetpion: " + e);
						return 0;
					}
				// ********************* string compare / subject *******************************************************/
				} else if (this.attrName.equals("subject")) {
					try {
						String attrName1 = "";
						String attrName2 = "";
						String routerId1 = ((IDfTypedObject)o1).getString("router_id");
						String routerId2 = ((IDfTypedObject)o2).getString("router_id");
						attrName1 = (routerId1.equals("0000000000000000")) ? "item_name" : "task_name";
						attrName2 = (routerId2.equals("0000000000000000")) ? "item_name" : "task_name";
						String string1 = ((IDfTypedObject)o1).getString(attrName1);
						String string2 = ((IDfTypedObject)o2).getString(attrName2);

						tmpResult = string1.compareTo(string2);
					
					} catch(Exception e) {
						Logger.getLogger().error("[search_inbox.jsp] String compare error! attrName:  " + this.attrName + " sortDirection: " + this.sortDirection + " Excetpion: " + e);
						return 0;
					}
				// ********************* string compare / docbase *******************************************************/
				} else if (this.attrName.equals("docbase")) {
					try {
						boolean inboxSentItemsOrWorkflowsFlag = ((IDfTypedObject)o1).hasAttr("item_id");
						String attrForDocbase = (inboxSentItemsOrWorkflowsFlag) ? "item_id" : "r_object_id";
						
						String id1 = ((IDfTypedObject)o1).getId(attrForDocbase).getDocbaseId();
						String id2 = ((IDfTypedObject)o2).getId(attrForDocbase).getDocbaseId();
						
						if (id1 == null || id1.equals("null") || id2 == null || id2.equals("null") || lm == null) return 0;
						
						String tmpId1 = Integer.decode("0x" + id1).toString();
						String tmpId2 = Integer.decode("0x" + id2).toString();
						
						IDfSession session1 = (IDfSession)lm.getActiveSession(tmpId1);
						IDfSession session2 = (IDfSession)lm.getActiveSession(tmpId2);
						
						String string1 = session1.getDocbaseName();
						String string2 = session2.getDocbaseName();

						tmpResult = string1.compareTo(string2);
					
					} catch(Exception e) {
						Logger.getLogger().error("[search_inbox.jsp] String compare error! attrName:  " + this.attrName + " sortDirection: " + this.sortDirection + " Excetpion: " + e);
						return 0;
					}
				// ********************* string compare / org_unit *******************************************************/
				} else if (this.attrName.equals("org_unit")) {
					try {
						OrgUnits ouManager = OrgUnits.getInstance(this.uiStrings.getLanguage());
    					Persons persons = Persons.getInstance(this.uiStrings.getLanguage());
    					
    					IDfTypedObject to1 = (IDfTypedObject)o1;
    					String objectDocbaseId1 = Integer.decode("0x" + to1.getObjectId().getDocbaseId()).toString();
    					IDfTypedObject to2 = (IDfTypedObject)o2;
    					String objectDocbaseId2 = Integer.decode("0x" + to2.getObjectId().getDocbaseId()).toString();
    					
						PersonBean supervisor1 = persons.getPersonByName(to1.getString("supervisor_name"), objectDocbaseId1);
						PersonBean supervisor2 = persons.getPersonByName(to2.getString("supervisor_name"), objectDocbaseId2);

    					String string1 = uiStrings.getLocalizedString("STR_UNKNOWN");
						String string2 = uiStrings.getLocalizedString("STR_UNKNOWN");
    					OrgUnitBean oub1 = ouManager.getOrgUnitById(supervisor1.getDefaultOrgUnit());
						OrgUnitBean oub2 = ouManager.getOrgUnitById(supervisor2.getDefaultOrgUnit());
    					if(oub1 != null) string1 = oub1.getName();
						if(oub2 != null) string2 = oub2.getName();
						
						tmpResult = string1.compareTo(string2);
					
					} catch(Exception e) {
						Logger.getLogger().error("[search_inbox.jsp] String compare error! attrName:  " + this.attrName + " sortDirection: " + this.sortDirection + " Excetpion: " + e);
						return 0;
					}
				// ********************* string compare / to, from, name, runtime_state, creator_name, supervisor_name *******************************************************/
				} else if (this.attrName.equals("to") || this.attrName.equals("from") || this.attrName.equals("name") || this.attrName.equals("runtime_state") || this.attrName.equals("creator_name") || this.attrName.equals("supervisor_name")) {
					try {
						if (this.attrName.equals("to")) realAttrName = "name";
						if (this.attrName.equals("from")) realAttrName = "sent_by";
						if (this.attrName.equals("name")) realAttrName = "object_name";
						if (this.attrName.equals("runtime_state")) realAttrName = "r_runtime_state";
						if (this.attrName.equals("creator_name")) realAttrName = "r_creator_name";
						if (this.attrName.equals("supervisor_name")) realAttrName = "supervisor_name";
						String string1 = ((IDfTypedObject)o1).getString(realAttrName);
						String string2 = ((IDfTypedObject)o2).getString(realAttrName);

						tmpResult = string1.compareTo(string2);
					
					} catch(Exception e) {
						Logger.getLogger().error("[search_inbox.jsp] String compare error! attrName:  " + this.attrName + " sortDirection: " + this.sortDirection + " Excetpion: " + e);
						return 0;
					}
				}
				
				if (tmpResult != 0 && this.sortDirection.equals("desc")) tmpResult = -tmpResult;	
				return tmpResult;
            }
        }
		
		// inner class used for sorting by sort order
        class SortItemsBySortOrder {
            public void sortItems(ArrayList allRows, DisplaySettings ds, LoginManager lm, ResourceStrings uiStrings) {
				ArrayList sortOrderList = ds.getSortOrderList();
				long starttime = System.currentTimeMillis();
				if(sortOrderList != null){
					for(int i = sortOrderList.size() - 1; i >= 0; i--){
						SortByBean sob = (SortByBean) sortOrderList.get(i);
						String attrName = sob.getAttrName();
						String sortDirection = sob.getSortDirection();
						
						SortItemsComparator s = new SortItemsComparator(attrName, sortDirection, lm, uiStrings);
						Collections.sort(allRows, s);
						Logger.getLogger().trace("[search_inbox.jsp] Sort attrName:    "  + attrName);

					}
				}
				long endtime = System.currentTimeMillis();
				Logger.getLogger().trace("[search_inbox.jsp] Sort time:    "  + (endtime - starttime));
			}
        }
		
        Logger.getLogger().info("[search_inbox.jsp] : Search successfully ended!");
        lm.loginToDocbase(currentDocbaseId);

		String destinationFile = "";
		SortItemsBySortOrder SortIt = new SortItemsBySortOrder();
		
		String additionalParams = "";
		if (destination.equals("inbox") && forPrint.equals("true")){
			DisplaySettings ds = pdb.getInfoCenterSettings().getInboxDS();
			SortIt.sortItems(allRows, ds, lm, uiStrings);
			destinationFile = "info_center/actions/print_inbox/print_inbox_items.jsp";
		} else if (destination.equals("inbox")){
			DisplaySettings ds = pdb.getInfoCenterSettings().getInboxDS();
			SortIt.sortItems(allRows, ds, lm, uiStrings);
			destinationFile = "info_center/inbox_items.jsp";
			//dpa - Task 382
			//if (!folderId.equals("all")) additionalParams = "&folderId=" + folderId;
			//if (sharedUserFlag) additionalParams = "&sharedInbox=true";
			additionalParams = "&folderId=" + folderId;
			if (sharedUserFlag) additionalParams += "&sharedInbox=true";
			//dpa - Task 382
		} else if (destination.equals("sent")) {
			DisplaySettings ds = pdb.getInfoCenterSettings().getSentItemsDS();
			SortIt.sortItems(allRows, ds, lm, uiStrings);
			destinationFile = "info_center/inbox_items.jsp";
		} else if (destination.equals("workflows") || destination.equals("workflows_express") || destination.equals("manager")) {
			DisplaySettings ds = pdb.getInfoCenterSettings().getMyWorkflowsDS();
			SortIt.sortItems(allRows, ds, lm, uiStrings);
			destinationFile = "info_center/workflows_items.jsp";
		} else if (destination.equals("inbox_express")){
			DisplaySettings ds = pdb.getInfoCenterSettings().getInboxDS();
			SortIt.sortItems(allRows, ds, lm, uiStrings);
			destinationFile = "info_center_express/items.jsp";
			if (sharedUserFlag) additionalParams = "&sharedInbox=true";
		} else if (destination.equals("sent_express")){
			DisplaySettings ds = pdb.getInfoCenterSettings().getSentItemsDS();
			SortIt.sortItems(allRows, ds, lm, uiStrings);
			destinationFile = "info_center_express/items.jsp";
		}
		
		Logger.getLogger().info("[search_inbox.jsp] : destinationFile: " + destinationFile);

		RequestDispatcher rd = application.getRequestDispatcher(jspPath + destinationFile + preContainerFilePath + additionalParams);
		request.setAttribute("result", allRows);
		rd.forward(request,response);
	
		result = null;
	} catch (Exception ex) {
		Logger.getLogger().error("[search_inbox.jsp] Error while performing inbox items search!" + ex.getMessage());
        lm.loginToDocbase(currentDocbaseId);
		throw new Exception("Error while performing inbox items search!" + ex.getMessage());
	}
	
	long endTime = System.currentTimeMillis();
	
	Logger.getLogger().trace("[search_inbox.jsp] total time = " + ( endTime - startTime ));
	Logger.getLogger().trace("[search_inbox.jsp] END");

%>
