1) Переменная PGDATA -> data postgres

2) ./pg_ctl start -> C:\Program Files\PostgreSQL\10\bin

3) pg_hba.conf -> # IPv4 local connections:
                 host    all             all             127.0.0.1/32            md5
                 host    all             all             *            md5
                 
                 //todo check it ^^^
4) Для винды надо создать переменную среды PGCLIENTENCODING -> win1251 (устанавливает кодировку для программы `psql`)

5) Также надо изменить кодировку для `cmd`, добавив команду при запуске оной в реестре (HKEY_CURRENT_USER\Software\Microsoft\Command Processor and add String value Autorun = chcp 1251) 
путь реестра не всегда дефолтный , корневая может быть другой , например HKEY_LOCAL_MACHINE\ . Искать просто Command Proccessor по всему реестру.

6) Для `gradle` задачи `bootRun` и `bootWar` требуются такие аргументы: `-Dspring.profiles.active=dev`, где `dev` - текущий профиль.

7) Чтобы русский текст работал через git-bash на винде надо создать файл настроек в папке юзера (типа `C:/Users/username`) так (`touch .bashrc`) и в него прописать локали:

    ```
    export LC_ALL=ru_RU.CP1251
    export LANG=ru_RU.CP1251
    export LANGUAGE=ru_RU.CP1251
    ```

8) При pg_dump надо не забывать про кодировку! И иногда надо несколько раз выполнить инсерты, т.к. всё валится на порядке добавления записей.

9) На сервере надо проверить кодировку системы. https://unix.stackexchange.com/a/303838/268799

10) To ignore changes in application-dev.yml execute:

    ```git update-index --assume-unchanged src\main\resources\application-dev.yml```
    
    to push changes execute:
    
    ```git update-index --no-assume-unchanged src\main\resources\application-dev.yml```

11) On tomcat9 you [should add](https://stackoverflow.com/a/56874245/3212712) `/var/lib/tomcat9/image` folder to `/lib/systemd/system/tomcat9.service` so:

    `ReadWritePaths=/var/lib/tomcat9/image/`
    
    and execute:
    
    `systemctl daemon-reload`  
    
    `service tomcat9 restart`