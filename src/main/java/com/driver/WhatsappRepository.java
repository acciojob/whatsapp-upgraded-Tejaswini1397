package com.driver;

import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class WhatsappRepository {
    HashMap<Group,List<User>>groupUserMap=new HashMap<>();
    HashMap<Group,List<Message>>groupMessMap=new HashMap<>();
    HashMap<Message,User>senderMap=new HashMap<>();
    HashMap<Group,User>adminMap=new HashMap<>();
    HashSet<String>userMobile=new HashSet<>();
    int customerCount;
    int messageId;

    public WhatsappRepository() {
        this.customerCount = 0;
        this.messageId = 0;
    }

    public String createUser(String name, String mobile) throws Exception {
        if(!userMobile.contains(mobile)){
            userMobile.add(mobile);
            User newUser=new User(name,mobile);
            return "SUCCESS";

        }
        throw new Exception("User already exists");
    }

    public Group createGroup(List<User> users) {
        if(users.size()==2){
            Group group=new Group(users.get(1).getName(),2);
            adminMap.put(group,users.get(0));
            groupUserMap.put(group,users);
            groupMessMap.put(group,new ArrayList<Message>());
            return group;
        }
        this.customerCount+=1;
        Group group=new Group(new String("Group "+this.customerCount),users.size());
        adminMap.put(group,users.get(0));
        groupUserMap.put(group,users);
        groupMessMap.put(group,new ArrayList<Message>());
        return group;
    }

    public int createMessage(String content) {
        this.messageId+=1;
        Message message=new Message(messageId,content);
        return messageId;
    }

    public int sendMessage(Message message, User sender, Group group) throws Exception {
        if(adminMap.containsKey(group)){
            List<User>userList=groupUserMap.get(group);
            boolean userFound=false;
            for(User user:userList){
                if(user.equals(sender)){
                    userFound=true;
                    break;
                }
            }
            if(userFound){
                senderMap.put(message,sender);
                List<Message>messageList=groupMessMap.get(group);
                messageList.add(message);
                groupMessMap.put(group,messageList);
                return messageList.size();

            }
            throw new Exception("You are not allowed to send message");
        }
        throw new Exception("Group does not exist");
    }

    public String changeAdmin(User approver, User user, Group group) throws Exception {
        if(adminMap.containsKey(group)){
            if(adminMap.get(group).equals(approver)){
                List<User>participant=groupUserMap.get(group);
                boolean useFound=false;
                for(User user1:participant){
                    if(user1.equals(user)){
                        useFound=true;
                        break;

                    }
                }
                if(useFound){
                    adminMap.put(group,user);
                    return "SUCCESS";
                }
                throw new Exception("User is not a participant");
            }
            throw new Exception("Approver does not have rights");
        }
        throw new Exception("Group does not exist");
    }

    public int removeUser(User user) throws Exception {
        boolean userFound=false;
        Group userGroup=null;
        for(Group group:groupUserMap.keySet()){
            List<User>participant=groupUserMap.get(group);
            for (User user1:participant){
                if(user1.equals(user)){
                    if(adminMap.get(group).equals(user)){
                        throw  new Exception("Cannot remove admin");
                    }
                    userGroup=group;
                    userFound=true;
                    break;
                }
            }
            if(userFound){
                break;
            }
        }
        if (userFound){
            List<User> users = groupUserMap.get(userGroup);
            List<User> updateUsers = new ArrayList<>();
            for (User participant : users) {
                if (participant.equals(user)) {
                    continue;
                }
                updateUsers.add(participant);
            }
            groupUserMap.put(userGroup, updateUsers);

            List<Message> messages = groupMessMap.get(userGroup);
            List<Message> updateMessages = new ArrayList<>();
            for (Message message : messages) {
                if (senderMap.get(message).equals(user))
                    continue;
                updateMessages.add(message);
            }
            groupMessMap.put(userGroup, updateMessages);
            HashMap<Message, User> updateSenderMap = new HashMap<>();
            for (Message message : senderMap.keySet()) {
                if (senderMap.get(message).equals(user))
                    continue;
                updateSenderMap.put(message, senderMap.get(message));
            }
            senderMap = updateSenderMap;
            return updateUsers.size()+ updateMessages.size()+ updateSenderMap.size();
        }
        throw new Exception("User not found");
    }

    public String findMessage(Date start, Date end, int k) throws Exception {
        List<Message> messageses = new ArrayList<>();
        for(Group group:groupMessMap.keySet()){
            messageses.addAll(groupMessMap.get(group));
        }
        List<Message>filterMessages=new ArrayList<>();
        for(Message message:messageses){
            if(message.getTimestamp().after(start) && message.getTimestamp().before(end)){
                filterMessages.add(message);
            }
        }

        if (filterMessages.size() < k) {
            throw new Exception("K is greater than the number of messages");
        }
        // Sort the messages in descending order of timestamp
        Collections.sort(filterMessages, new Comparator<Message>() {
            @Override
            public int compare(Message o1, Message o2) {
                return o2.getTimestamp().compareTo(o1.getTimestamp());
            }
        });

        return filterMessages.get(k-1).getContent();
    }

}
