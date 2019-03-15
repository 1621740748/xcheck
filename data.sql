create table blocked_resources(
  res_id int auto_increment primary key comment '资源ID' ,
  res_url varchar(1024) comment '链接url',
  res_host varchar(256) comment '链接host',
  res_host_path varchar(1024) comment '链接host path',
  page_url varchar(1024) comment '所在页面',
  initiate_url varchar(4096) comment '发起调用的资源url',
  create_time timestamp default CURRENT_TIMESTAMP comment '创建时间'
);