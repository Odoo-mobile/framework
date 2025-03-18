# Changelog

### Legacy Changelog
- November 2016
    - odoo-rpc-v3 source merged with Framework
    - Added Odoo 10.0 support
    - Minor bug fixes
    - Runtime model registry (No need to register each models in BaseModel class)
      Note: With Instant Run registry will not work in Debug mode, but it will work fine in Released mode
      *Disable Instant Run https://goo.gl/lsfhq9*

- June 2016
    - Sync performance improved
    - Easy relation record creation (Fix for: [https://github.com/Odoo-mobile/framework/issues/138](138))
        - ManyToOne, OneToMany and ManyToMany
    - Minor bug fixes
